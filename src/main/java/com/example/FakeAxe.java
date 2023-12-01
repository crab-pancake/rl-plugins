package com.example;

import java.util.ArrayList;
import java.util.List;
import net.runelite.api.RuneLiteObject;
import net.runelite.api.coords.WorldPoint;

public class FakeAxe
{
	public RuneLiteObject axe;
	public List<WorldPoint> path;
	public WorldPoint nextPoint;
	public int stepX = 0;
	public int stepY = 0;

	public FakeAxe(RuneLiteObject axe, WorldPoint swCorner, int spot){
		this.axe = axe;
		this.path = tendrilNumToPath(spot, swCorner);
		this.nextPoint = path.get(0);
	}

	private List<WorldPoint> tendrilNumToPath(int i, WorldPoint arenaSWCorner){
		List<WorldPoint> path = new ArrayList<>();

		// TODO: add more paths for funky axes
		switch(i){
			case 0: // BL to TR
				path.add(arenaSWCorner.dx(2).dy(2));
				path.add(arenaSWCorner.dx(4).dy(4));
				path.add(arenaSWCorner.dx(6).dy(6));
				path.add(arenaSWCorner.dx(8).dy(8));
				path.add(arenaSWCorner.dx(9).dy(9));
			case 1: // Bot mid to top mid
				path.add(arenaSWCorner.dx(5).dy(2));
				path.add(arenaSWCorner.dx(5).dy(4));
				path.add(arenaSWCorner.dx(5).dy(6));
				path.add(arenaSWCorner.dx(5).dy(8));
				path.add(arenaSWCorner.dx(5).dy(9));
			case 2: // BR to TL
				path.add(arenaSWCorner.dx(8).dy(2));
				path.add(arenaSWCorner.dx(6).dy(4));
				path.add(arenaSWCorner.dx(4).dy(6));
				path.add(arenaSWCorner.dx(2).dy(8));
				path.add(arenaSWCorner.dx(1).dy(9));
			case 3: // mid left to mid right
				path.add(arenaSWCorner.dx(2).dy(5));
				path.add(arenaSWCorner.dx(4).dy(5));
				path.add(arenaSWCorner.dx(6).dy(5));
				path.add(arenaSWCorner.dx(8).dy(5));
				path.add(arenaSWCorner.dx(9).dy(5));
			case 4: // mid right to mid left
				path.add(arenaSWCorner.dx(8).dy(5));
				path.add(arenaSWCorner.dx(6).dy(5));
				path.add(arenaSWCorner.dx(4).dy(5));
				path.add(arenaSWCorner.dx(2).dy(5));
				path.add(arenaSWCorner.dx(1).dy(5));
			case 5: // TL to BR
				path.add(arenaSWCorner.dx(2).dy(8));
				path.add(arenaSWCorner.dx(4).dy(6));
				path.add(arenaSWCorner.dx(6).dy(4));
				path.add(arenaSWCorner.dx(8).dy(2));
				path.add(arenaSWCorner.dx(9).dy(1));
			case 6: // Top mid to bot mid
				path.add(arenaSWCorner.dx(5).dy(8));
				path.add(arenaSWCorner.dx(5).dy(6));
				path.add(arenaSWCorner.dx(5).dy(4));
				path.add(arenaSWCorner.dx(5).dy(2));
				path.add(arenaSWCorner.dx(5).dy(1));
			case 7: // TR to BL
			default:
				path.add(arenaSWCorner.dx(8).dy(8));
				path.add(arenaSWCorner.dx(6).dy(6));
				path.add(arenaSWCorner.dx(4).dy(4));
				path.add(arenaSWCorner.dx(2).dy(2));
				path.add(arenaSWCorner.dx(1).dy(1));
		}
		return path;
	}
}
