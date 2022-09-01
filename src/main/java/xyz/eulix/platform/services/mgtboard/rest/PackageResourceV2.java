package xyz.eulix.platform.services.mgtboard.rest;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import xyz.eulix.platform.services.mgtboard.dto.*;
import xyz.eulix.platform.services.mgtboard.service.PkgMgtService;
import xyz.eulix.platform.services.support.log.Logged;
import xyz.eulix.platform.services.support.model.BatchDeleteResult;
import xyz.eulix.platform.services.support.model.PageListResult;
import xyz.eulix.platform.services.support.model.SortDirEnum;
import xyz.eulix.platform.services.support.validator.ValueOfEnum;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.*;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Package Rest类
 */
@ApplicationScoped
@Path("/v2/service")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Platform Package Management Service", description = "软件包版本管理APIv2")
public class PackageResourceV2 {

    @Inject
    PkgMgtService pkgMgtService;

    @GET
    @Path("/packages/compatibility/check")
    @Logged
    @Operation(description = "检查app、box版本兼容性")
    public CompatibleCheckRes pkgCompatibleCheck(@NotBlank @Parameter(required = true) @HeaderParam("Request-Id") String requestId,
                                                 @NotBlank @Parameter(required = true) @QueryParam("app_pkg_name") String appPkgName,
                                                 @NotNull @ValueOfEnum(enumClass = PkgTypeEnum.class, valueMethod = "getName")
                                                 @Parameter(required = true, schema = @Schema(enumeration = {"android", "ios"}))
                                                 @QueryParam("app_pkg_type") String appPkgType,
                                                 @NotBlank @Parameter(required = true) @QueryParam("box_pkg_name") String boxPkgName,
                                                 @NotNull @ValueOfEnum(enumClass = PkgTypeEnum.class, valueMethod = "getName")
                                                 @Parameter(required = true, schema = @Schema(enumeration = {"box"}))
                                                 @QueryParam("box_pkg_type") String boxPkgType,
                                                 @NotNull @Pattern(regexp = "[a-zA-Z0-9.-]{0,50}") @QueryParam("cur_box_version") String curBoxVersion,
                                                 @NotNull @Pattern(regexp = "[a-zA-Z0-9.-]{0,50}") @QueryParam("cur_app_version") String curAppVersion) {
        return pkgMgtService.compatibleCheck(appPkgName, appPkgType, curAppVersion, boxPkgName, boxPkgType, curBoxVersion);
    }

    @GET
    @Path("/packages/version/check")
    @Logged
    @Operation(description = "检查软件包版本更新")
    public PackageCheckRes packageCheck(@NotBlank @Parameter(required = true) @HeaderParam("Request-Id") String requestId,
                                        @NotBlank @Parameter(required = true, schema = @Schema(enumeration = {"app_check", "box_check"}))
                                        @ValueOfEnum(enumClass = PkgActionEnum.class, valueMethod = "getName") @QueryParam("action") String action,
                                        @NotBlank @Parameter(required = true) @QueryParam("app_pkg_name") String appName,
                                        @NotBlank @Parameter(required = true) @QueryParam("box_pkg_name") String boxName,
                                        @NotNull @ValueOfEnum(enumClass = PkgTypeEnum.class, valueMethod = "getName")
                                        @Parameter(required = true, schema = @Schema(enumeration = {"android", "ios"}))
                                        @QueryParam("app_pkg_type") String appType,
                                        @NotNull @Parameter(required = true, schema = @Schema(enumeration = {"box"}))
                                        @ValueOfEnum(enumClass = PkgTypeEnum.class, valueMethod = "getName") @QueryParam("box_pkg_type") String boxType,
                                        @NotNull @Pattern(regexp = "[a-zA-Z0-9.-]{0,50}") @QueryParam("cur_box_version") String curBoxVersion,
                                        @NotNull @Pattern(regexp = "[a-zA-Z0-9.-]{0,50}") @QueryParam("cur_app_version") String curAppVersion) {

        PkgActionEnum actionEnum = PkgActionEnum.fromValue(action);
        switch (actionEnum) {
            case APP_CHECK:
                return pkgMgtService.checkAppInfo(appName, appType, curAppVersion, boxName, boxType, curBoxVersion);
            case BOX_CHECK:
                return pkgMgtService.checkBoxInfo(appName, appType, curAppVersion, boxName, boxType, curBoxVersion);
            default:
                throw new UnsupportedOperationException();
        }
    }

    @GET
    @Path("/packages/box/latest")
    @Logged
    @Operation(description = "检查 box 最新版本")
    public PackageRes packageBoxCheck(@NotBlank @Parameter(required = true) @HeaderParam("Request-Id") String requestId,
                                      @NotBlank @QueryParam("box_pkg_name") String boxName, @NotNull @Parameter(required = true, schema = @Schema(enumeration = {"box"}))
                                      @ValueOfEnum(enumClass = PkgTypeEnum.class, valueMethod = "getName") @QueryParam("box_pkg_type") String boxType) {
        return pkgMgtService.getBoxLatestVersion(boxName, boxType);
    }

    @GET
    @Path("/packages/app/latest")
    @Logged
    @Operation(description = "检查 app 最新版本")
    public PackageRes packageAppCheck(@NotBlank @Parameter(required = true) @HeaderParam("Request-Id") String requestId,
                                      @NotBlank @QueryParam("app_pkg_name") String appName, @NotNull @ValueOfEnum(enumClass = PkgTypeEnum.class, valueMethod = "getName")
                                      @Parameter(required = true, schema = @Schema(enumeration = {"android", "ios"})) @QueryParam("app_pkg_type") String appType) {
        return pkgMgtService.getAppLatestVersion(appName, appType);
    }

    @RolesAllowed("admin")
    @POST
    @Path("/packages")
    @Logged
    @Operation(description = "增加软件包版本，需管理员权限")
    public PackageRes packageSave(@NotBlank @Parameter(required = true) @HeaderParam("Request-Id") String requestId,
                                  @Valid PackageReq packageReq) {
        return pkgMgtService.savePkgInfo(packageReq);
    }

    @RolesAllowed("admin")
    @PUT
    @Path("/packages/{package_id}")
    @Logged
    @Operation(description = "更新软件包版本，需管理员权限")
    public PackageRes packageUpdate(@NotBlank @Parameter(required = true) @HeaderParam("Request-Id") String requestId,
                                    @NotBlank @PathParam("package_id") String packageId,
                                    @Valid PackageReq packageReq) {
        return pkgMgtService.updatePkginfo(packageReq);
    }

    @RolesAllowed("admin")
    @GET
    @Path("/packages/{package_id}")
    @Logged
    @Operation(description = "查询 pkg 详情，需管理员权限")
    public PackageRes packageGet(@NotBlank @Parameter(required = true) @HeaderParam("Request-Id") String requestId,
                                 @NotBlank @PathParam("package_id") String packageId) {
        return null;
    }

    @RolesAllowed("admin")
    @GET
    @Path("/packages")
    @Logged
    @Operation(description = "查询软件包列表，需管理员权限")
    public PageListResult<PackageRes> packageList(@NotBlank @Parameter(required = true) @HeaderParam("Request-Id") String requestId,
                                                  @ValueOfEnum(enumClass = SortKeyEnum.class, valueMethod = "getName")
                                                  @Parameter(schema = @Schema(enumeration = {"pkg_type"})) @QueryParam("sort_key") String sortKey,
                                                  @ValueOfEnum(enumClass = SortDirEnum.class, valueMethod = "getName")
                                                  @Parameter(schema = @Schema(enumeration = {"asc", "desc"})) @QueryParam("sort_dir") String sortDir,
                                                  @Parameter(required = true, description = "当前页") @QueryParam("current_page") Integer currentPage,
                                                  @Parameter(required = true, description = "每页数量，最大2000") @Max(2000) @QueryParam("page_size") Integer pageSize) {
        return pkgMgtService.listPackage(sortKey, sortDir, currentPage, pageSize);
    }

    @RolesAllowed("admin")
    @DELETE
    @Path("/packages")
    @Logged
    @Operation(description = "批量删除软件包版本，需管理员权限")
    public BatchDeleteResult packagesDel(@NotBlank @Parameter(required = true) @HeaderParam("Request-Id") String requestId,
                                         @Size(min = 1, max = 2000) @QueryParam("package_ids") List<@NotNull Long> packageIds) {
        pkgMgtService.delPkginfos(packageIds);
        return BatchDeleteResult.of();
    }
}
