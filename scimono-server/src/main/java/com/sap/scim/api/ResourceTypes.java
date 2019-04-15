
package com.sap.scim.api;

import static com.sap.scim.api.API.APPLICATION_JSON_SCIM;
import static com.sap.scim.helper.Resources.addLocation;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import com.sap.scim.SCIMApplication;
import com.sap.scim.callback.config.SCIMConfigurationCallback;
import com.sap.scim.callback.resourcetype.ResourceTypesCallback;
import com.sap.scim.entity.Group;
import com.sap.scim.entity.Meta;
import com.sap.scim.entity.User;
import com.sap.scim.entity.paging.PagedByIndexSearchResult;
import com.sap.scim.entity.paging.PagedResult;
import com.sap.scim.entity.schema.ResourceType;
import com.sap.scim.entity.schema.Schema;
import com.sap.scim.entity.schema.ResourceType.Builder;
import com.sap.scim.exception.ResourceNotFoundException;

@Path(API.RESOURCE_TYPES)
@Produces(APPLICATION_JSON_SCIM)
@Consumes(APPLICATION_JSON_SCIM)
public class ResourceTypes {
  private static final Instant now = Instant.now();

  //@formatter:off
  private static final ResourceType RESOURCE_TYPE_USER = new Builder()
      .setId(User.RESOURCE_TYPE_USER)
      .name(User.RESOURCE_TYPE_USER)
      .description("User Account")
      .endpoint(API.USERS)
      .schema(User.SCHEMA)
      .setMeta(new Meta.Builder(now, now)
                 .setResourceType(ResourceType.RESOURCE_TYPE_RESOURCE_TYPE).build())
      .build();

  private static final ResourceType RESOURCE_TYPE_GROUP = new Builder()
      .setId(Group.RESOURCE_TYPE_GROUP)
      .name(Group.RESOURCE_TYPE_GROUP)
      .description("Group")
      .endpoint(API.GROUPS)
      .schema(Group.SCHEMA)
      .setMeta(new Meta.Builder(now, now)
                  .setResourceType(ResourceType.RESOURCE_TYPE_RESOURCE_TYPE).build())
      .build();

  private static final ResourceType RESOURCE_TYPE_SCHEMA = new Builder()
      .setId(Schema.RESOURCE_TYPE_SCHEMA)
      .name(Schema.RESOURCE_TYPE_SCHEMA)
      .description("Schema")
      .endpoint(API.SCHEMAS)
      .schema(Schema.SCHEMA)
      .setMeta(new Meta.Builder(now, now)
                  .setResourceType(ResourceType.RESOURCE_TYPE_RESOURCE_TYPE).build())
      .build();
  //@formatter:on

  @Context
  private UriInfo uriInfo;

  private final SCIMConfigurationCallback scimConfig;
  private final ResourceTypesCallback resourceTypesCallback;

  public ResourceTypes(@Context Application appContext) {
    SCIMApplication scimApplication = SCIMApplication.from(appContext);

    resourceTypesCallback = scimApplication.getResourceTypesCallback();
    scimConfig = scimApplication.getConfigurationCallback();
  }

  @GET
  public PagedByIndexSearchResult<ResourceType> getResourceTypes() {
    List<ResourceType> resources = new ArrayList<>();

    ResourceType userResourceType = addLocation(RESOURCE_TYPE_USER, uriInfo.getAbsolutePathBuilder().path(User.RESOURCE_TYPE_USER));
    resources.add(userResourceType);

    ResourceType groupResourceType = addLocation(RESOURCE_TYPE_GROUP, uriInfo.getAbsolutePathBuilder().path(Group.RESOURCE_TYPE_GROUP));
    resources.add(groupResourceType);

    ResourceType schemaResourceType = addLocation(RESOURCE_TYPE_SCHEMA, uriInfo.getAbsolutePathBuilder().path(Schema.RESOURCE_TYPE_SCHEMA));
    resources.add(schemaResourceType);

    PagedResult<ResourceType> customResourceTypes = resourceTypesCallback.getCustomResourceTypes();
    addLocation(customResourceTypes, uriInfo);

    resources.addAll(customResourceTypes.getResources());

    return new PagedByIndexSearchResult<>(resources, resources.size(), scimConfig.getMaxResourcesPerPage(), new Long(1));
  }

  @GET
  @Path("{id}")
  public ResourceType getResourceType(@PathParam("id") final String typeId) {
    ResourceType resourceType;

    switch (typeId) {
      case User.RESOURCE_TYPE_USER:
        resourceType = RESOURCE_TYPE_USER;
        break;

      case Group.RESOURCE_TYPE_GROUP:
        resourceType = RESOURCE_TYPE_GROUP;
        break;

      case Schema.RESOURCE_TYPE_SCHEMA:
        resourceType = RESOURCE_TYPE_SCHEMA;
        break;

      default:
        resourceType = resourceTypesCallback.getCustomResourceType(typeId);
    }

    if (resourceType == null) {
      throw new ResourceNotFoundException(ResourceType.RESOURCE_TYPE_RESOURCE_TYPE, typeId);
    }

    return addLocation(resourceType, uriInfo.getAbsolutePath());
  }
}
