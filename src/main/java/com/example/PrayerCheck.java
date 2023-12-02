package com.example;

import javax.annotation.Nullable;

public class PrayerCheck
{
	public int tick;
	public int prayerToCheck;
	@Nullable
	public Integer soundId;
	@Nullable
	public Integer graphicId;
	public PrayerCheck(int tick, int prayer, Integer soundId, Integer graphicId){
		this.tick = tick;
		this.soundId = soundId;
		this.graphicId = graphicId;
		this.prayerToCheck = prayer;
	}
	public PrayerCheck(int tick, int prayer){
		this.tick = tick;
		this.prayerToCheck = prayer;
	}
}
