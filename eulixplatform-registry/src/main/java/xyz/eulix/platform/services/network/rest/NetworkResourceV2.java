package xyz.eulix.platform.services.network.rest;

import javax.validation.Valid;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import xyz.eulix.platform.services.network.dto.BaseResultRes;
import xyz.eulix.platform.services.network.dto.NetworkAuthReqV2;
import xyz.eulix.platform.services.network.dto.NetworkServerRes;
import xyz.eulix.platform.services.network.dto.StunServerRes;
import xyz.eulix.platform.services.network.service.NetworkService;
import xyz.eulix.platform.common.support.log.Logged;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.constraints.NotBlank;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@RequestScoped
@Path("/v2/platform")
@Tag(name = "Platform Network Manage Service", description = "网络管控APIv2")
public class NetworkResourceV2 {
    private static final Logger LOG = Logger.getLogger("app.log");

    @Inject
    NetworkService networkService;

    @Logged
    @POST
    @Path("/clients/network/auth")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "认证 network client 身份")
    public BaseResultRes networkClientAuth(@HeaderParam("Request-Id") @NotBlank String reqId,
        @Valid NetworkAuthReqV2 networkAuthReqV2) {
        Boolean result = networkService.networkClientAuth(networkAuthReqV2.getNetworkClientId(), networkAuthReqV2.getNetworkSecretKey());
        return BaseResultRes.of(result);
    }

    @Logged
    @GET
    @Path("/servers/network/detail")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "查询最新 network server 信息")
    public NetworkServerRes networkServerDetail(@HeaderParam("Request-Id") @NotBlank String reqId,
                                                @QueryParam("network_client_id") @NotBlank String networkClientId) {
        return networkService.networkServerDetail(networkClientId);
    }

    @Logged
    @GET
    @Path("/servers/stun/detail")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "查询相应 stun server 信息")
    public StunServerRes stunServerDetail(@HeaderParam("Request-Id") @NotBlank String reqId,
                                          @QueryParam("subdomain") @NotBlank String subdomain) {
        return networkService.stunServerDetail(subdomain);
    }
}