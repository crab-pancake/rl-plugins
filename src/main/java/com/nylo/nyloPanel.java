package com.nylo;

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
public class nyloPanel extends PluginPanel {
	private static final Logger log = LoggerFactory.getLogger(nyloPanel.class);
	private final nyloPlugin plugin;
	private final nyloConfig config;

	@Inject
	public nyloPanel(nyloPlugin plugin, nyloConfig config) {
		this.plugin = plugin;
		this.config = config;
		this.setLayout(new BorderLayout(0, 4));
		this.setBackground(ColorScheme.DARK_GRAY_COLOR);
		this.setBorder(new EmptyBorder(8, 8, 8, 8));
		JPanel mainContent = new JPanel(new BorderLayout());
	}
}