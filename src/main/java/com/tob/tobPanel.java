package com.tob;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

@Singleton
public class tobPanel extends PluginPanel {
	private static final Logger log = LoggerFactory.getLogger(tobPanel.class);
	private final tobPlugin plugin;
	private final tobConfig config;

	@Inject
	public tobPanel(tobPlugin plugin, tobConfig config) {
		this.plugin = plugin;
		this.config = config;
		this.setLayout(new BorderLayout(0, 4));
		this.setBackground(ColorScheme.DARK_GRAY_COLOR);
		this.setBorder(new EmptyBorder(8, 8, 8, 8));
		JPanel mainContent = new JPanel(new BorderLayout());
	}
}