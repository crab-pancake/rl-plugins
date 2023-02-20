package com.hitsplattracker;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static net.runelite.client.RuneLite.RUNELITE_DIR;

@Slf4j
public class HitsplatTrackerWriter {
    public void toFile(String filename, String contents)
    {
        File dir = new File(RUNELITE_DIR, "dmg-tracker/");

        dir.mkdirs();

        try (FileWriter fw = new FileWriter(new File(dir, filename+".log")))
        {
            fw.write(contents);
        }
        catch (IOException ex)
        {
            log.debug("Error writing file: {}", ex.getMessage());
        }
    }
}
