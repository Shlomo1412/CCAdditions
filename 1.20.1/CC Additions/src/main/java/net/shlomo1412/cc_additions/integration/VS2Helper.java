package net.shlomo1412.cc_additions.integration;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for VS2 integration using reflection.
 * This allows compilation without VS2 dependency while supporting VS2 at runtime.
 * 
 * When VS2 is loaded, this class uses reflection to access VS2 APIs.
 * All VS2-specific types are handled via reflection to avoid ClassNotFoundError.
 */
public class VS2Helper {

    private static boolean initialized = false;
    private static boolean vs2Available = false;
    
    // Cached reflection objects
    private static Class<?> vsGameUtilsClass;
    private static Method getShipManagingPosMethod;
    private static Method getShipIdMethod;
    private static Method getShipSlugMethod;
    private static Method getShipTransformMethod;
    private static Method getVelocityMethod;
    private static Method getOmegaMethod;
    private static Method getInertiaDataMethod;
    private static Method isStaticMethod;
    
    private static void initialize() {
        if (initialized) return;
        initialized = true;
        
        try {
            // Try to load VS2 classes
            vsGameUtilsClass = Class.forName("org.valkyrienskies.mod.common.VSGameUtilsKt");
            getShipManagingPosMethod = vsGameUtilsClass.getMethod("getShipManagingPos", Level.class, BlockPos.class);
            
            Class<?> shipClass = Class.forName("org.valkyrienskies.core.api.ships.Ship");
            getShipIdMethod = shipClass.getMethod("getId");
            getShipSlugMethod = shipClass.getMethod("getSlug");
            getShipTransformMethod = shipClass.getMethod("getTransform");
            getVelocityMethod = shipClass.getMethod("getVelocity");
            getOmegaMethod = shipClass.getMethod("getOmega");
            getInertiaDataMethod = shipClass.getMethod("getInertiaData");
            isStaticMethod = shipClass.getMethod("isStatic");
            
            vs2Available = true;
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            vs2Available = false;
        }
    }

    /**
     * Get the ship at the given block position, or null if not on a ship.
     */
    @Nullable
    private static Object getShipAt(Level level, BlockPos pos) {
        initialize();
        if (!vs2Available) return null;
        
        try {
            return getShipManagingPosMethod.invoke(null, level, pos);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Check if the given position is on a ship.
     */
    public static boolean isOnShip(Level level, BlockPos pos) {
        return getShipAt(level, pos) != null;
    }

    /**
     * Get the ship ID, or -1 if not on a ship.
     */
    public static long getShipId(Level level, BlockPos pos) {
        Object ship = getShipAt(level, pos);
        if (ship == null) return -1;
        
        try {
            return (Long) getShipIdMethod.invoke(ship);
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Get the ship name/slug, or null if not on a ship.
     */
    @Nullable
    public static String getShipName(Level level, BlockPos pos) {
        Object ship = getShipAt(level, pos);
        if (ship == null) return null;
        
        try {
            return (String) getShipSlugMethod.invoke(ship);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get all ship data as a map for Lua.
     */
    public static Map<String, Object> getAllShipData(Level level, BlockPos pos) {
        Map<String, Object> data = new HashMap<>();
        Object ship = getShipAt(level, pos);
        
        if (ship == null) {
            data.put("isOnShip", false);
            return data;
        }

        try {
            data.put("isOnShip", true);
            data.put("id", getShipIdMethod.invoke(ship));
            data.put("name", getShipSlugMethod.invoke(ship));

            // Transform data
            Object transform = getShipTransformMethod.invoke(ship);
            if (transform != null) {
                Method getPositionInWorld = transform.getClass().getMethod("getPositionInWorld");
                Object worldPos = getPositionInWorld.invoke(transform);
                if (worldPos != null) {
                    Method xMethod = worldPos.getClass().getMethod("x");
                    Method yMethod = worldPos.getClass().getMethod("y");
                    Method zMethod = worldPos.getClass().getMethod("z");
                    
                    Map<String, Double> position = new HashMap<>();
                    position.put("x", (Double) xMethod.invoke(worldPos));
                    position.put("y", (Double) yMethod.invoke(worldPos));
                    position.put("z", (Double) zMethod.invoke(worldPos));
                    data.put("position", position);
                }

                Method getShipToWorldRotation = transform.getClass().getMethod("getShipToWorldRotation");
                Object rotation = getShipToWorldRotation.invoke(transform);
                if (rotation != null) {
                    Method wMethod = rotation.getClass().getMethod("w");
                    Method xMethod = rotation.getClass().getMethod("x");
                    Method yMethod = rotation.getClass().getMethod("y");
                    Method zMethod = rotation.getClass().getMethod("z");
                    
                    Map<String, Double> quat = new HashMap<>();
                    quat.put("w", (Double) wMethod.invoke(rotation));
                    quat.put("x", (Double) xMethod.invoke(rotation));
                    quat.put("y", (Double) yMethod.invoke(rotation));
                    quat.put("z", (Double) zMethod.invoke(rotation));
                    data.put("rotation", quat);
                    
                    // Euler angles
                    double qw = (Double) wMethod.invoke(rotation);
                    double qx = (Double) xMethod.invoke(rotation);
                    double qy = (Double) yMethod.invoke(rotation);
                    double qz = (Double) zMethod.invoke(rotation);
                    
                    Map<String, Double> euler = quaternionToEuler(qw, qx, qy, qz);
                    data.put("rotationEuler", euler);
                }
            }

            // Velocity
            Object vel = getVelocityMethod.invoke(ship);
            if (vel != null) {
                Method xMethod = vel.getClass().getMethod("x");
                Method yMethod = vel.getClass().getMethod("y");
                Method zMethod = vel.getClass().getMethod("z");
                
                Map<String, Double> velocity = new HashMap<>();
                velocity.put("x", (Double) xMethod.invoke(vel));
                velocity.put("y", (Double) yMethod.invoke(vel));
                velocity.put("z", (Double) zMethod.invoke(vel));
                data.put("velocity", velocity);
            }

            // Angular velocity
            Object omega = getOmegaMethod.invoke(ship);
            if (omega != null) {
                Method xMethod = omega.getClass().getMethod("x");
                Method yMethod = omega.getClass().getMethod("y");
                Method zMethod = omega.getClass().getMethod("z");
                
                Map<String, Double> angularVelocity = new HashMap<>();
                angularVelocity.put("x", (Double) xMethod.invoke(omega));
                angularVelocity.put("y", (Double) yMethod.invoke(omega));
                angularVelocity.put("z", (Double) zMethod.invoke(omega));
                data.put("angularVelocity", angularVelocity);
            }

            // Inertia data
            Object inertiaData = getInertiaDataMethod.invoke(ship);
            if (inertiaData != null) {
                Method getMass = inertiaData.getClass().getMethod("getMass");
                data.put("mass", getMass.invoke(inertiaData));
            }

            // Is static
            data.put("isStatic", isStaticMethod.invoke(ship));

        } catch (Exception e) {
            // If anything fails, just return what we have
        }

        return data;
    }

    private static Map<String, Double> quaternionToEuler(double w, double x, double y, double z) {
        Map<String, Double> euler = new HashMap<>();
        
        // Roll (x-axis rotation)
        double sinr_cosp = 2 * (w * x + y * z);
        double cosr_cosp = 1 - 2 * (x * x + y * y);
        double roll = Math.atan2(sinr_cosp, cosr_cosp);
        
        // Pitch (y-axis rotation)
        double sinp = 2 * (w * y - z * x);
        double pitch;
        if (Math.abs(sinp) >= 1) {
            pitch = Math.copySign(Math.PI / 2, sinp);
        } else {
            pitch = Math.asin(sinp);
        }
        
        // Yaw (z-axis rotation)
        double siny_cosp = 2 * (w * z + x * y);
        double cosy_cosp = 1 - 2 * (y * y + z * z);
        double yaw = Math.atan2(siny_cosp, cosy_cosp);
        
        euler.put("pitch", Math.toDegrees(pitch));
        euler.put("yaw", Math.toDegrees(yaw));
        euler.put("roll", Math.toDegrees(roll));
        
        return euler;
    }

    /**
     * Get position in world coordinates.
     */
    public static Map<String, Double> getPosition(Level level, BlockPos pos) {
        Map<String, Object> allData = getAllShipData(level, pos);
        Object position = allData.get("position");
        if (position instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Double> posMap = (Map<String, Double>) position;
            return posMap;
        }
        return null;
    }

    /**
     * Get rotation as quaternion.
     */
    public static Map<String, Double> getRotation(Level level, BlockPos pos) {
        Map<String, Object> allData = getAllShipData(level, pos);
        Object rotation = allData.get("rotation");
        if (rotation instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Double> rotMap = (Map<String, Double>) rotation;
            return rotMap;
        }
        return null;
    }

    /**
     * Get rotation as euler angles (degrees).
     */
    public static Map<String, Double> getRotationEuler(Level level, BlockPos pos) {
        Map<String, Object> allData = getAllShipData(level, pos);
        Object euler = allData.get("rotationEuler");
        if (euler instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Double> eulerMap = (Map<String, Double>) euler;
            return eulerMap;
        }
        return null;
    }

    /**
     * Get linear velocity.
     */
    public static Map<String, Double> getVelocity(Level level, BlockPos pos) {
        Map<String, Object> allData = getAllShipData(level, pos);
        Object velocity = allData.get("velocity");
        if (velocity instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Double> velMap = (Map<String, Double>) velocity;
            return velMap;
        }
        return null;
    }

    /**
     * Get angular velocity.
     */
    public static Map<String, Double> getAngularVelocity(Level level, BlockPos pos) {
        Map<String, Object> allData = getAllShipData(level, pos);
        Object omega = allData.get("angularVelocity");
        if (omega instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Double> omegaMap = (Map<String, Double>) omega;
            return omegaMap;
        }
        return null;
    }

    /**
     * Get ship mass in kg.
     */
    public static double getMass(Level level, BlockPos pos) {
        Map<String, Object> allData = getAllShipData(level, pos);
        Object mass = allData.get("mass");
        if (mass instanceof Number) {
            return ((Number) mass).doubleValue();
        }
        return 0;
    }

    /**
     * Check if ship physics is disabled (static).
     */
    public static boolean isStatic(Level level, BlockPos pos) {
        Map<String, Object> allData = getAllShipData(level, pos);
        Object isStatic = allData.get("isStatic");
        if (isStatic instanceof Boolean) {
            return (Boolean) isStatic;
        }
        return false;
    }

    // ==================== SHIP SCANNING ====================

    /**
     * Get all ships in the level as a list of data maps.
     * Uses VS2's ship world API via reflection.
     */
    public static List<Map<String, Object>> getAllShips(Level level) {
        initialize();
        List<Map<String, Object>> ships = new ArrayList<>();
        if (!vs2Available) return ships;

        try {
            // VSGameUtilsKt.getShipObjectWorld(level) -> ShipObjectWorld
            Method getShipObjectWorld = vsGameUtilsClass.getMethod("getShipObjectWorld", Level.class);
            Object shipWorld = getShipObjectWorld.invoke(null, level);
            if (shipWorld == null) return ships;

            // ShipObjectWorld extends QueryableShipData or has getAllShips/getLoadedShips
            // Try getAllShips() first, then getLoadedShips()
            Method getAllShipsMethod = null;
            for (Method m : shipWorld.getClass().getMethods()) {
                if (m.getName().equals("getAllShips") && m.getParameterCount() == 0) {
                    getAllShipsMethod = m;
                    break;
                }
                if (m.getName().equals("getLoadedShips") && m.getParameterCount() == 0) {
                    getAllShipsMethod = m;
                }
            }
            if (getAllShipsMethod == null) return ships;

            Object shipsIterable = getAllShipsMethod.invoke(shipWorld);
            if (!(shipsIterable instanceof Iterable<?>)) return ships;

            for (Object ship : (Iterable<?>) shipsIterable) {
                Map<String, Object> shipData = buildShipDataFromObject(ship);
                if (shipData != null) {
                    ships.add(shipData);
                }
            }
        } catch (Exception e) {
            // Reflection failed, return what we have
        }

        return ships;
    }

    /**
     * Build a ship data map from a Ship object using reflection.
     */
    @Nullable
    private static Map<String, Object> buildShipDataFromObject(Object ship) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("id", getShipIdMethod.invoke(ship));
            data.put("name", getShipSlugMethod.invoke(ship));

            // Transform -> position
            Object transform = getShipTransformMethod.invoke(ship);
            if (transform != null) {
                Method getPositionInWorld = transform.getClass().getMethod("getPositionInWorld");
                Object worldPos = getPositionInWorld.invoke(transform);
                if (worldPos != null) {
                    Method xM = worldPos.getClass().getMethod("x");
                    Method yM = worldPos.getClass().getMethod("y");
                    Method zM = worldPos.getClass().getMethod("z");
                    Map<String, Double> position = new HashMap<>();
                    position.put("x", (Double) xM.invoke(worldPos));
                    position.put("y", (Double) yM.invoke(worldPos));
                    position.put("z", (Double) zM.invoke(worldPos));
                    data.put("position", position);
                }

                Method getShipToWorldRotation = transform.getClass().getMethod("getShipToWorldRotation");
                Object rotation = getShipToWorldRotation.invoke(transform);
                if (rotation != null) {
                    Method wM = rotation.getClass().getMethod("w");
                    Method xM = rotation.getClass().getMethod("x");
                    Method yM = rotation.getClass().getMethod("y");
                    Method zM = rotation.getClass().getMethod("z");
                    Map<String, Double> quat = new HashMap<>();
                    quat.put("w", (Double) wM.invoke(rotation));
                    quat.put("x", (Double) xM.invoke(rotation));
                    quat.put("y", (Double) yM.invoke(rotation));
                    quat.put("z", (Double) zM.invoke(rotation));
                    data.put("rotation", quat);

                    double qw = (Double) wM.invoke(rotation);
                    double qx = (Double) xM.invoke(rotation);
                    double qy = (Double) yM.invoke(rotation);
                    double qz = (Double) zM.invoke(rotation);
                    data.put("rotationEuler", quaternionToEuler(qw, qx, qy, qz));
                }
            }

            // Velocity
            Object vel = getVelocityMethod.invoke(ship);
            if (vel != null) {
                Method xM = vel.getClass().getMethod("x");
                Method yM = vel.getClass().getMethod("y");
                Method zM = vel.getClass().getMethod("z");
                Map<String, Double> velocity = new HashMap<>();
                velocity.put("x", (Double) xM.invoke(vel));
                velocity.put("y", (Double) yM.invoke(vel));
                velocity.put("z", (Double) zM.invoke(vel));
                data.put("velocity", velocity);
            }

            // Angular velocity
            Object omega = getOmegaMethod.invoke(ship);
            if (omega != null) {
                Method xM = omega.getClass().getMethod("x");
                Method yM = omega.getClass().getMethod("y");
                Method zM = omega.getClass().getMethod("z");
                Map<String, Double> angVel = new HashMap<>();
                angVel.put("x", (Double) xM.invoke(omega));
                angVel.put("y", (Double) yM.invoke(omega));
                angVel.put("z", (Double) zM.invoke(omega));
                data.put("angularVelocity", angVel);
            }

            // Mass
            Object inertiaData = getInertiaDataMethod.invoke(ship);
            if (inertiaData != null) {
                Method getMass = inertiaData.getClass().getMethod("getMass");
                data.put("mass", getMass.invoke(inertiaData));
            }

            // Is static
            data.put("isStatic", isStaticMethod.invoke(ship));

            return data;
        } catch (Exception e) {
            return null;
        }
    }

    // ==================== CONTROL METHODS ====================
    // These require ServerShip and are more complex with reflection
    // For now, they return false indicating operation not supported without direct VS2 dependency

    /**
     * Apply a force in world coordinates.
     * Note: Requires VS2 compile dependency for full functionality.
     */
    public static boolean applyWorldForce(ServerLevel level, BlockPos pos, double x, double y, double z) {
        // TODO: Implement with reflection when VS2 compile dependency is available
        return false;
    }

    /**
     * Apply a torque in world coordinates.
     */
    public static boolean applyWorldTorque(ServerLevel level, BlockPos pos, double x, double y, double z) {
        return false;
    }

    /**
     * Apply a force in ship-relative (model) coordinates.
     */
    public static boolean applyModelForce(ServerLevel level, BlockPos pos, double x, double y, double z) {
        return false;
    }

    /**
     * Apply a torque in ship-relative (model) coordinates.
     */
    public static boolean applyModelTorque(ServerLevel level, BlockPos pos, double x, double y, double z) {
        return false;
    }

    /**
     * Apply a force at a specific position on the ship (ship coordinates).
     */
    public static boolean applyForceAtPosition(ServerLevel level, BlockPos pos, 
            double fx, double fy, double fz, double px, double py, double pz) {
        return false;
    }

    /**
     * Set the ship's linear velocity directly.
     */
    public static boolean setVelocity(ServerLevel level, BlockPos pos, double x, double y, double z) {
        return false;
    }

    /**
     * Set the ship's angular velocity directly.
     */
    public static boolean setAngularVelocity(ServerLevel level, BlockPos pos, double x, double y, double z) {
        return false;
    }

    /**
     * Add to the ship's current linear velocity.
     */
    public static boolean addVelocity(ServerLevel level, BlockPos pos, double x, double y, double z) {
        return false;
    }

    /**
     * Add to the ship's current angular velocity.
     */
    public static boolean addAngularVelocity(ServerLevel level, BlockPos pos, double x, double y, double z) {
        return false;
    }

    /**
     * Enable or disable physics for the ship.
     */
    public static boolean setStatic(ServerLevel level, BlockPos pos, boolean isStatic) {
        return false;
    }
}
