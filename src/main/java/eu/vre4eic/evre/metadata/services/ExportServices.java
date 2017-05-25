/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.vre4eic.evre.metadata.services;

import eu.vre4eic.evre.blazegraph.BlazegraphRepRestful;
import eu.vre4eic.evre.core.Common.MetadataOperationType;
import eu.vre4eic.evre.core.Common.ResponseStatus;
import eu.vre4eic.evre.core.comm.Publisher;
import eu.vre4eic.evre.core.comm.PublisherFactory;
import eu.vre4eic.evre.core.messages.MetadataMessage;
import eu.vre4eic.evre.core.messages.impl.MetadataMessageImpl;
import eu.vre4eic.evre.metadata.utils.PropertiesManager;
import eu.vre4eic.evre.nodeservice.modules.authentication.AuthModule;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * REST Web Service
 *
 * @author rousakis
 */
@Path("export")
public class ExportServices {

    PropertiesManager propertiesManager = PropertiesManager.getPropertiesManager();
    String namespace = propertiesManager.getTripleStoreNamespace();
    @Context
    private UriInfo context;
    @Context
    private HttpServletRequest requestContext;
    private BlazegraphRepRestful blazegraphRepRestful;
    private AuthModule module;
    private Publisher<MetadataMessage> mdp;

    /**
     * Creates a new instance of ExportServices
     */
    public ExportServices() {
    }

    @PostConstruct
    public void initialize() {
        blazegraphRepRestful = new BlazegraphRepRestful(propertiesManager.getTripleStoreUrl());
        module = AuthModule.getInstance("tcp://v4e-lab.isti.cnr.it:61616");
        mdp = PublisherFactory.getMetatdaPublisher();
    }

    @GET
    public Response exportFileGETJSON(@QueryParam("graph") String graph,
            @QueryParam("format") String format,
            @DefaultValue("") @QueryParam("token") String token) throws ParseException, IOException {
        String namespace = this.namespace;
        return execExportGET(token, format, graph, namespace);
    }

    public Response execExportGET(String token, String format, String graph, String namespace) throws UnsupportedEncodingException {
        int status;
        String authToken = requestContext.getHeader("Authorization");
        if (authToken == null) {
            authToken = token;
        }
        boolean isTokenValid = module.checkToken(authToken);
//        isTokenValid = true;
        MetadataMessageImpl message = new MetadataMessageImpl();
        message.setOperation(MetadataOperationType.READ);
        message.setToken(authToken);
        Response response = null;
        if (!isTokenValid) {
            message.setMessage("User not authenticated!");
            message.setStatus(ResponseStatus.FAILED);
            status = 401;
        } else if (format == null) {
            message.setStatus(ResponseStatus.FAILED);
            status = 400;
            message.setMessage("Error in the provided format.");
        } else {
            response = blazegraphRepRestful.exportFile(format, namespace, graph);
            status = response.getStatus();
            if (status == 200) {
                message.setStatus(ResponseStatus.SUCCEED);
                message.setMessage("Data were exported successfully. ");
            } else {
                message.setStatus(ResponseStatus.FAILED);
                message.setMessage(response.readEntity(String.class));
            }
        }
        mdp.publish(message);
        if (status == 200) {
            return Response.status(status).entity(response.readEntity(String.class)).header("Access-Control-Allow-Origin", "*").build();
        } else {
            return Response.status(status).entity(message.toJSON()).header("Access-Control-Allow-Origin", "*").build();
        }
    }

    @GET
    @Path("/namespace/{namespace}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response queryExecGETJSONWithNS(
            @PathParam("namespace") String namespace,
            @QueryParam("graph") String graph,
            @QueryParam("format") String format,
            @DefaultValue("") @QueryParam("token") String token) throws ParseException, IOException {
        return execExportGET(token, format, graph, namespace);
    }

//    @POST
//    @Consumes(MediaType.APPLICATION_JSON)
//    public Response exportFilePOSTJSON(String jsonInput,
//            @DefaultValue("") @QueryParam("token") String token) throws ParseException, IOException {
//        JSONParser jsonParser = new JSONParser();
//        JSONObject jsonObject = (JSONObject) jsonParser.parse(jsonInput);
//        int status;
//        String authToken = requestContext.getHeader("Authorization");
//        if (authToken == null) {
//            authToken = token;
//        }
//        boolean isTokenValid = module.checkToken(authToken);
//        isTokenValid = true;
//        MetadataMessageImpl message = new MetadataMessageImpl();
//        message.setOperation(MetadataOperationType.READ);
//        message.setToken(authToken);
//        String format = null;
//        String graph = null;
//        Response response = null;
//        if (!isTokenValid) {
//            message.setMessage("User not authenticated!");
//            message.setStatus(ResponseStatus.FAILED);
//            status = 401;
//        } else if (jsonObject.size() != 2) {
//            message.setMessage("JSON input message should have exactly 2 arguments.");
//            message.setStatus(ResponseStatus.FAILED);
//            status = 400;
//        } else {
//            format = (String) jsonObject.get("format");
//            graph = (String) jsonObject.get("graph");
//            if (format == null) {
//                message.setStatus(ResponseStatus.FAILED);
//                status = 400;
//                message.setMessage("Error in the provided format.");
//            } else {
//                response = blazegraphRepRestful.exportFile(format, namespace, graph);
//                status = response.getStatus();
//                if (status == 200) {
//                    message.setStatus(ResponseStatus.SUCCEED);
//                    message.setMessage("Data were exported successfully. ");
//                } else {
//                    message.setStatus(ResponseStatus.FAILED);
//                    message.setMessage(response.readEntity(String.class));
//                }
//            }
//        }
//        mdp.publish(message);
//        if (status == 200) {
//            return Response.status(status).entity(response.readEntity(String.class)).header("Access-Control-Allow-Origin", "*").build();
//        } else {
//            return Response.status(status).entity(message.toJSON()).header("Access-Control-Allow-Origin", "*").build();
//        }
//    }
}
