package org.janelia.render.service;

import java.net.UnknownHostException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import org.janelia.alignment.RenderParameters;
import org.janelia.alignment.spec.stack.StackId;
import org.janelia.alignment.spec.stack.StackMetaData;
import org.janelia.render.service.util.RenderServiceUtil;
import org.janelia.render.service.util.ResponseHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * Tile centric APIs for rendering images.
 *
 * @author Eric Trautman
 */
@Path("/v1/owner/{owner}")
@Api(tags = {"Tile Image APIs"})
public class TileImageService {

    private final RenderDataService renderDataService;
    private final TileDataService tileDataService;

    @SuppressWarnings("UnusedDeclaration")
    public TileImageService()
            throws UnknownHostException {
        this(new RenderDataService(),
             new TileDataService());
    }

    public TileImageService(final RenderDataService renderDataService,
                            final TileDataService tileDataService) {
        this.renderDataService = renderDataService;
        this.tileDataService = tileDataService;
    }

    @Path("project/{project}/stack/{stack}/tile/{tileId}/scale/{scale}/jpeg-image")
    @GET
    @Produces(RenderServiceUtil.IMAGE_JPEG_MIME_TYPE)
    @ApiOperation(value = "Render JPEG image for a tile")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Tile not found")
    })
    public Response renderJpegImageForTile(@PathParam("owner") final String owner,
                                           @PathParam("project") final String project,
                                           @PathParam("stack") final String stack,
                                           @PathParam("tileId") final String tileId,
                                           @PathParam("scale") final Double scale,
                                           @QueryParam("filter") final Boolean filter,
                                           @Context final Request request) {

        LOG.info("renderJpegImageForTile: entry, owner={}, project={}, stack={}, tileId={}, scale={}, filter={}",
                 owner, project, stack, tileId, scale, filter);

        final ResponseHelper responseHelper = new ResponseHelper(request, getStackMetaData(owner, project, stack));
        if (responseHelper.isModified()) {
            final RenderParameters renderParameters =
                    tileDataService.getRenderParameters(owner, project, stack, tileId, scale, filter, false);
            return RenderServiceUtil.renderJpegImage(renderParameters, false, responseHelper);
        } else {
            return responseHelper.getNotModifiedResponse();
        }
    }

    @Path("project/{project}/stack/{stack}/tile/{tileId}/scale/{scale}/png-image")
    @GET
    @Produces(RenderServiceUtil.IMAGE_PNG_MIME_TYPE)
    @ApiOperation(value = "Render PNG image for a tile")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Tile not found")
    })
    public Response renderPngImageForTile(@PathParam("owner") final String owner,
                                          @PathParam("project") final String project,
                                          @PathParam("stack") final String stack,
                                          @PathParam("tileId") final String tileId,
                                          @PathParam("scale") final Double scale,
                                          @QueryParam("filter") final Boolean filter,
                                          @Context final Request request) {

        LOG.info("renderPngImageForTile: entry, owner={}, project={}, stack={}, tileId={}, scale={}, filter={}",
                 owner, project, stack, tileId, scale, filter);

        final ResponseHelper responseHelper = new ResponseHelper(request, getStackMetaData(owner, project, stack));
        if (responseHelper.isModified()) {
            final RenderParameters renderParameters =
                    tileDataService.getRenderParameters(owner, project, stack, tileId, scale, filter, false);
            return RenderServiceUtil.renderPngImage(renderParameters, false, responseHelper);
        } else {
            return responseHelper.getNotModifiedResponse();
        }
    }

    @Path("project/{project}/stack/{stack}/tile/{tileId}/scale/{scale}/tiff-image")
    @GET
    @Produces(RenderServiceUtil.IMAGE_TIFF_MIME_TYPE)
    @ApiOperation(value = "Render TIFF image for a tile")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Tile not found")
    })
    public Response renderTiffImageForTile(@PathParam("owner") final String owner,
                                           @PathParam("project") final String project,
                                           @PathParam("stack") final String stack,
                                           @PathParam("tileId") final String tileId,
                                           @PathParam("scale") final Double scale,
                                           @QueryParam("filter") final Boolean filter,
                                           @Context final Request request) {

        LOG.info("renderTiffImageForTile: entry, owner={}, project={}, stack={}, tileId={}, scale={}, filter={}",
                 owner, project, stack, tileId, scale, filter);

        final ResponseHelper responseHelper = new ResponseHelper(request, getStackMetaData(owner, project, stack));
        if (responseHelper.isModified()) {
            final RenderParameters renderParameters =
                    tileDataService.getRenderParameters(owner, project, stack, tileId, scale, filter, false);
            return RenderServiceUtil.renderTiffImage(renderParameters, false, responseHelper);
        } else {
            return responseHelper.getNotModifiedResponse();
        }
    }

    @Path("project/{project}/stack/{stack}/tile/{tileId}/source/scale/{scale}/jpeg-image")
    @GET
    @Produces(RenderServiceUtil.IMAGE_JPEG_MIME_TYPE)
    @ApiOperation(value = "Render tile's source image without transformations in JPEG format")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Tile not found")
    })
    public Response renderJpegSourceImageForTile(@PathParam("owner") final String owner,
                                                 @PathParam("project") final String project,
                                                 @PathParam("stack") final String stack,
                                                 @PathParam("tileId") final String tileId,
                                                 @PathParam("scale") final Double scale,
                                                 @QueryParam("filter") final Boolean filter,
                                                 @Context final Request request) {

        LOG.info("renderJpegSourceImageForTile: entry, owner={}, project={}, stack={}, tileId={}, scale={}, filter={}",
                 owner, project, stack, tileId, scale, filter);

        final ResponseHelper responseHelper = new ResponseHelper(request, getStackMetaData(owner, project, stack));
        if (responseHelper.isModified()) {
            final RenderParameters renderParameters =
                    tileDataService.getTileSourceRenderParameters(owner, project, stack, tileId, scale, filter);
            return RenderServiceUtil.renderJpegImage(renderParameters, false, responseHelper);
        } else {
            return responseHelper.getNotModifiedResponse();
        }
    }

    @Path("project/{project}/stack/{stack}/tile/{tileId}/source/scale/{scale}/png-image")
    @GET
    @Produces(RenderServiceUtil.IMAGE_PNG_MIME_TYPE)
    @ApiOperation(value = "Render tile's source image without transformations in PNG format")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Tile not found")
    })
    public Response renderPngSourceImageForTile(@PathParam("owner") final String owner,
                                                @PathParam("project") final String project,
                                                @PathParam("stack") final String stack,
                                                @PathParam("tileId") final String tileId,
                                                @PathParam("scale") final Double scale,
                                                @QueryParam("filter") final Boolean filter,
                                                @Context final Request request) {

        LOG.info("renderPngSourceImageForTile: entry, owner={}, project={}, stack={}, tileId={}, scale={}, filter={}",
                 owner, project, stack, tileId, scale, filter);

        final ResponseHelper responseHelper = new ResponseHelper(request, getStackMetaData(owner, project, stack));
        if (responseHelper.isModified()) {
            final RenderParameters renderParameters =
                    tileDataService.getTileSourceRenderParameters(owner, project, stack, tileId, scale, filter);
            return RenderServiceUtil.renderPngImage(renderParameters, false, responseHelper);
        } else {
            return responseHelper.getNotModifiedResponse();
        }
    }

    @Path("project/{project}/stack/{stack}/tile/{tileId}/source/scale/{scale}/tiff-image")
    @GET
    @Produces(RenderServiceUtil.IMAGE_TIFF_MIME_TYPE)
    @ApiOperation(value = "Render tile's source image without transformations in TIFF format")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Tile not found")
    })
    public Response renderTiffSourceImageForTile(@PathParam("owner") final String owner,
                                                 @PathParam("project") final String project,
                                                 @PathParam("stack") final String stack,
                                                 @PathParam("tileId") final String tileId,
                                                 @PathParam("scale") final Double scale,
                                                 @QueryParam("filter") final Boolean filter,
                                                 @Context final Request request) {

        LOG.info("renderTiffSourceImageForTile: entry, owner={}, project={}, stack={}, tileId={}, scale={}, filter={}",
                 owner, project, stack, tileId, scale, filter);

        final ResponseHelper responseHelper = new ResponseHelper(request, getStackMetaData(owner, project, stack));
        if (responseHelper.isModified()) {
            final RenderParameters renderParameters =
                    tileDataService.getTileSourceRenderParameters(owner, project, stack, tileId, scale, filter);
            return RenderServiceUtil.renderTiffImage(renderParameters, false, responseHelper);
        } else {
            return responseHelper.getNotModifiedResponse();
        }
    }

    @Path("project/{project}/stack/{stack}/tile/{tileId}/mask/scale/{scale}/jpeg-image")
    @GET
    @Produces(RenderServiceUtil.IMAGE_JPEG_MIME_TYPE)
    @ApiOperation(value = "Render tile's mask image without transformations in JPEG format")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Tile not found")
    })
    public Response renderJpegMaskImageForTile(@PathParam("owner") final String owner,
                                               @PathParam("project") final String project,
                                               @PathParam("stack") final String stack,
                                               @PathParam("tileId") final String tileId,
                                               @PathParam("scale") final Double scale,
                                               @QueryParam("filter") final Boolean filter,
                                               @Context final Request request) {

        LOG.info("renderJpegMaskImageForTile: entry, owner={}, project={}, stack={}, tileId={}, scale={}, filter={}",
                 owner, project, stack, tileId, scale, filter);

        final ResponseHelper responseHelper = new ResponseHelper(request, getStackMetaData(owner, project, stack));
        if (responseHelper.isModified()) {
            final RenderParameters renderParameters =
                    tileDataService.getTileMaskRenderParameters(owner, project, stack, tileId, scale, filter);
            return RenderServiceUtil.renderJpegImage(renderParameters, false, responseHelper);
        } else {
            return responseHelper.getNotModifiedResponse();
        }
    }

    @Path("project/{project}/stack/{stack}/tile/{tileId}/mask/scale/{scale}/png-image")
    @GET
    @Produces(RenderServiceUtil.IMAGE_PNG_MIME_TYPE)
    @ApiOperation(value = "Render tile's mask image without transformations in PNG format")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Tile not found")
    })
    public Response renderPngMaskImageForTile(@PathParam("owner") final String owner,
                                              @PathParam("project") final String project,
                                              @PathParam("stack") final String stack,
                                              @PathParam("tileId") final String tileId,
                                              @PathParam("scale") final Double scale,
                                              @QueryParam("filter") final Boolean filter,
                                              @Context final Request request) {

        LOG.info("renderPngMaskImageForTile: entry, owner={}, project={}, stack={}, tileId={}, scale={}, filter={}",
                 owner, project, stack, tileId, scale, filter);

        final ResponseHelper responseHelper = new ResponseHelper(request, getStackMetaData(owner, project, stack));
        if (responseHelper.isModified()) {
            final RenderParameters renderParameters =
                    tileDataService.getTileMaskRenderParameters(owner, project, stack, tileId, scale, filter);
            return RenderServiceUtil.renderPngImage(renderParameters, false, responseHelper);
        } else {
            return responseHelper.getNotModifiedResponse();
        }
    }

    @Path("project/{project}/stack/{stack}/tile/{tileId}/mask/scale/{scale}/tiff-image")
    @GET
    @Produces(RenderServiceUtil.IMAGE_TIFF_MIME_TYPE)
    @ApiOperation(value = "Render tile's mask image without transformations in TIFF format")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Tile not found")
    })
    public Response renderTiffMaskImageForTile(@PathParam("owner") final String owner,
                                               @PathParam("project") final String project,
                                               @PathParam("stack") final String stack,
                                               @PathParam("tileId") final String tileId,
                                               @PathParam("scale") final Double scale,
                                               @QueryParam("filter") final Boolean filter,
                                               @Context final Request request) {

        LOG.info("renderTiffMaskImageForTile: entry, owner={}, project={}, stack={}, tileId={}, scale={}, filter={}",
                 owner, project, stack, tileId, scale, filter);

        final ResponseHelper responseHelper = new ResponseHelper(request, getStackMetaData(owner, project, stack));
        if (responseHelper.isModified()) {
            final RenderParameters renderParameters =
                    tileDataService.getTileMaskRenderParameters(owner, project, stack, tileId, scale, filter);
            return RenderServiceUtil.renderTiffImage(renderParameters, false, responseHelper);
        } else {
            return responseHelper.getNotModifiedResponse();
        }
    }

    private StackMetaData getStackMetaData(final String owner,
                                           final String project,
                                           final String stack) {
        final StackId stackId = new StackId(owner, project, stack);
        return renderDataService.getStackMetaData(stackId);
    }

    private static final Logger LOG = LoggerFactory.getLogger(TileImageService.class);
}
