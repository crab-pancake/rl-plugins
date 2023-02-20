package com.lineofsight;

import net.runelite.api.*;
import net.runelite.api.coords.Direction;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

import javax.inject.Inject;
import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static com.lineofsight.LineOfSightConfig.Origin.PLAYER;
import static java.awt.Color.*;
import static java.lang.Math.abs;
import static java.lang.Math.sqrt;
import static net.runelite.api.coords.Direction.*;
import static com.lineofsight.LineOfSightConfig.LineOfSightMode.*;

public class LineOfSightOverlay extends Overlay {
    private final Client client;
    private final LineOfSightConfig config;
    HashMap<WorldPoint, Boolean> losTiles;

    @Inject
    private LineOfSightOverlay(Client client, LineOfSightPlugin plugin, LineOfSightConfig config){
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
        this.client = client;
        this.config = config;
    }

    @Override
    public Dimension render(Graphics2D graphics){
        if (config.lineOfSight()){
            renderLineOfSight(graphics);
        }
        if (config.hoveredTile() || config.interacting())
            if (client.getLocalPlayer() != null && client.getCollisionMaps() != null && client.getSelectedSceneTile() != null)
                {
                Scene scene = client.getScene();
                Tile[][][] tiles = scene.getTiles();

                int z = client.getPlane();

                if (config.hoveredTile()) {
                    if (config.origin() == PLAYER)
					{
						losTiles = hasLos(client.getLocalPlayer().getWorldLocation(), WorldPoint.fromLocal(client, client.getSelectedSceneTile().getLocalLocation()));
					}
                    else losTiles = hasLos(WorldPoint.fromLocal(client, client.getSelectedSceneTile().getLocalLocation()), client.getLocalPlayer().getWorldLocation());


                    for (int x = 0; x < Constants.SCENE_SIZE; ++x) {
                        for (int y = 0; y < Constants.SCENE_SIZE; ++y) {
                            Tile tile = tiles[z][x][y];
                            if (tile == null) continue;
                            Player player = client.getLocalPlayer();
                            if (player == null) continue;
                            Polygon poly = Perspective.getCanvasTilePoly(client, tile.getLocalLocation());
                            if (poly == null) continue;
                            renderLosTiles(graphics, tile);
                        }
                    }
                }

                if (config.interacting() && client.getLocalPlayer().getInteracting() != null) {
                    if (config.origin() == PLAYER)
					{
						losTiles = hasLos(client.getLocalPlayer().getWorldLocation(), client.getLocalPlayer().getInteracting().getWorldLocation());
					}
					else
					{
						losTiles = hasLos(client.getLocalPlayer().getInteracting().getWorldLocation(), client.getLocalPlayer().getWorldLocation());
					}

                    for (int x = 0; x < Constants.SCENE_SIZE; ++x) {
                        for (int y = 0; y < Constants.SCENE_SIZE; ++y) {
                            Tile tile = tiles[z][x][y];
                            if (tile == null) continue;
                            Player player = client.getLocalPlayer();
                            if (player == null) continue;
                            Polygon poly = Perspective.getCanvasTilePoly(client, tile.getLocalLocation());
                            if (poly == null) continue;
                            renderLosTiles(graphics, tile);
                        }
                    }
                }
            }
        return null;
    }

    private void renderTileIfHasLineOfSight(Graphics2D graphics, WorldArea start, int targetX, int targetY, boolean drawDest, boolean outOfRange)
    {
        WorldPoint targetLocation = new WorldPoint(targetX, targetY, start.getPlane());

        // Running the line of sight algorithm 100 times per frame doesn't
        // seem to use much CPU time, however rendering 100 tiles does
        if (start.hasLineOfSightTo(client, targetLocation))
        {
            LocalPoint lp = LocalPoint.fromWorld(client, drawDest ? targetLocation : new WorldPoint(start.getX(), start.getY(), start.getPlane()));
            if (lp == null)
            {
                return;
            }

            Polygon poly = Perspective.getCanvasTilePoly(client, lp);
            if (poly == null)
            {
                return;
            }

			OverlayUtil.renderPolygon(graphics, poly, outOfRange ? Color.CYAN.darker() : Color.BLUE, new BasicStroke(1));
        }
    }

    private void renderLineOfSight(Graphics2D graphics)
    {
        WorldArea playerLoc = client.getLocalPlayer().getWorldArea();
        for (int x = playerLoc.getX() - 15; x <= playerLoc.getX() + 15; x++)
        {
            for (int y = playerLoc.getY() - 15; y <= playerLoc.getY() + 15; y++)
            {
                if (x == playerLoc.getX() && y == playerLoc.getY())
                {
                    continue;
                }
                if (config.mode() == SIGHT) {
					boolean outOfRange = Math.abs(playerLoc.getX() - x) > 10 || Math.abs(playerLoc.getY() - y) > 10;
                    if (config.origin() == PLAYER) {
                        renderTileIfHasLineOfSight(graphics, playerLoc, x, y, true, outOfRange);
                    }
                    else {
                        WorldArea origin = new WorldArea(x, y, 1, 1, client.getPlane());
                        renderTileIfHasLineOfSight(graphics, origin, client.getLocalPlayer().getWorldLocation().getX(), client.getLocalPlayer().getWorldLocation().getY(), false, outOfRange);
                    }
                }
                else if (config.mode() == WALK){
                    if (config.origin() == PLAYER) {
                        renderTileIfHasLineOfWalk(graphics, client.getLocalPlayer().getWorldLocation(), x, y, true);
                    }
                    else {
                        WorldPoint b = new WorldPoint(x, y, client.getPlane());
                        renderTileIfHasLineOfWalk(graphics, b, client.getLocalPlayer().getWorldLocation().getX(), client.getLocalPlayer().getWorldLocation().getY(), false);
                    }
                }
            }
        }
    }

    private void renderTileIfHasLineOfWalk(Graphics2D graphics, WorldPoint start, int targetX, int targetY, boolean drawDest)
    {
        WorldPoint targetLocation = new WorldPoint(targetX, targetY, start.getPlane());

        // Running the line of sight algorithm 100 times per frame doesn't
        // seem to use much CPU time, however rendering 100 tiles does

        LocalPoint lp = LocalPoint.fromWorld(client, drawDest ? targetLocation : start);
        if (lp == null)
        {
            return;
        }

        Polygon poly = Perspective.getCanvasTilePoly(client, lp);
        if (poly == null)
        {
            return;
        }

        // could do this way more efficiently but cba for now
        if (!hasLos(start, targetLocation).containsValue(false))
            OverlayUtil.renderPolygon(graphics, poly, BLUE, new BasicStroke(1));
    }

    public HashMap<WorldPoint, Boolean> hasLos(WorldPoint origin, WorldPoint destination)
    {
        // basically all stolen from https://playtechs.blogspot.com/2007/03/raytracing-on-grid.html

        HashMap<WorldPoint, Boolean> tiles = new HashMap<>();
        int x0 = origin.getX();
        int y0 = origin.getY();
        int x1 = destination.getX();
        int y1 = destination.getY();
        int dx = abs(x1 - x0);
        int dy = abs(y1 - y0);
        int x = x0;
        int y = y0;
        int n = 1 + dx + dy;
        int x_inc = (x1 > x0) ? 1 : -1;
        int y_inc = (y1 > y0) ? 1 : -1;
        int error = dx - dy;
        Direction direction = null;

        if (dx > dy){
            error += dy;
        }
        else { // if dx == dy then go vertical first also
            error -= dx;
        }

        dx *= 2;
        dy *= 2;

        for (; n > 0; n--)
        {
            boolean passable = true;
            // visit(x,y):
            LocalPoint localPoint = LocalPoint.fromWorld(client, x, y);
            if (localPoint != null && localPoint.isInScene() && client.getCollisionMaps() != null)
            {
                int[][] flags = client.getCollisionMaps()[client.getPlane()].getFlags();
                int data = flags[localPoint.getSceneX()][localPoint.getSceneY()];

				// TODO: refactor

                Set<MovementFlag> movementFlags = MovementFlag.getSetFlags(data);

                if (!movementFlags.isEmpty()) {
                    if (config.mode() == WALK){
                        if (movementFlags.contains(MovementFlag.BLOCK_MOVEMENT_FLOOR) || movementFlags.contains(MovementFlag.BLOCK_MOVEMENT_FLOOR_DECORATION)
                                || movementFlags.contains(MovementFlag.BLOCK_MOVEMENT_FULL) || movementFlags.contains(MovementFlag.BLOCK_MOVEMENT_OBJECT)) {
                                passable = false;
                        }
                    }
                    if (movementFlags.contains(MovementFlag.BLOCK_LINE_OF_SIGHT_FULL)){
                        passable = false;
                    }
                }

                if (n > 1) // don't do directional check on the last tile. if it gets this far then it has succeeded
                {
                    if (error > 0) {
                        x += x_inc;
                        error -= dy;
                        direction = x_inc > 0 ? EAST : WEST;
                    }
                    else if (error == 0) {
                        int denominator = GCD(dx, dy);

                        // denominator of reduced fraction has at least 2 distinct prime factors -> longest axis first
                        if (distinctPrimeFactors(denominator).size() > 1) {
                            if (dx > dy) { // if dx == dy then error will never == 0 since we go back and forth between +dx and -dx
                                x += x_inc;
                                error -= dy;
                                direction = x_inc > 0 ? EAST : WEST;
                            } else {
                                y += y_inc;
                                error += dx;
                                direction = y_inc > 0 ? Direction.NORTH : SOUTH;
                            }
                        }

                        // denominator of reduced fraction has 1 prime factor -> shortest axis first
                        else if (distinctPrimeFactors(denominator).size() == 1) {
                            if (dx < dy) {
                                x += x_inc;
                                error -= dy;
                                direction = x_inc > 0 ? EAST : WEST;
                            } else {
                                y += y_inc;
                                error += dx;
                                direction = y_inc > 0 ? Direction.NORTH : SOUTH;
                            }
                        }
                    }
                    else {
                        y += y_inc;
                        error += dx;
                        direction = y_inc > 0 ? Direction.NORTH : SOUTH;
                    }

                    if (passable && direction != null && !movementFlags.isEmpty())
					{ // don't do this check if this tile is already blocked
                        // visit(x,y)
                        // checking the next direction you head in isn't blocked
						MovementFlag mf;
                        switch (direction) {
                            case NORTH:
								mf = config.mode() == WALK ? MovementFlag.BLOCK_MOVEMENT_NORTH : MovementFlag.BLOCK_LINE_OF_SIGHT_NORTH;
                            case SOUTH:
								mf = config.mode() == WALK ? MovementFlag.BLOCK_MOVEMENT_SOUTH : MovementFlag.BLOCK_LINE_OF_SIGHT_SOUTH;
                                break;
                            case EAST:
								mf = config.mode() == WALK ? MovementFlag.BLOCK_MOVEMENT_EAST : MovementFlag.BLOCK_LINE_OF_SIGHT_EAST;
                                break;
                            default:
								mf = config.mode() == WALK ? MovementFlag.BLOCK_MOVEMENT_WEST : MovementFlag.BLOCK_LINE_OF_SIGHT_WEST;
                        }
						if (movementFlags.contains(mf)){
							passable = false;
						}
                    }
                }

                tiles.put(WorldPoint.fromLocal(client, localPoint), passable);
            }
        }
        return tiles;
    }

    private void renderLosTiles(Graphics2D graphics, Tile tile){
        if (losTiles.containsKey(tile.getWorldLocation())) {
            Polygon poly = Perspective.getCanvasTilePoly(client, tile.getLocalLocation());

            if (poly == null) {
                return;
            }

            OverlayUtil.renderPolygon(graphics, poly, losTiles.get(tile.getWorldLocation()) ? GREEN : RED, new BasicStroke(1));
        }
    }

    static int GCD(int a, int b){
        if (b==0) return a;
        return GCD(b,a%b);
    }

    static HashSet<Integer> distinctPrimeFactors(int a){
//        int sqrt = (int) sqrt(a);
        HashSet<Integer> factors = new HashSet<>();
        for (int i = 2; i <= sqrt(a); i++) {
            if (a % i == 0) {
                factors.add(i);
                while (a % i == 0){
                    a /= i;
                }
            }
        }
        if (a != 1) {
            factors.add(a);
        }
        return factors;
    }

}
