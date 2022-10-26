package xyz.eulix.platform.services.registry.dto.registry;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Used to define a data transfer object as REST response for holding related result
 * of registry reset result.
 */
@Data(staticConstructor = "of")
public class BoxRegistryResetResult {
  @Schema(description = "盒子的 UUID")
  private final String boxUUID;
}