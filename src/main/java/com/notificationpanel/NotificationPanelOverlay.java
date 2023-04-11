package com.notificationpanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.inject.Inject;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import static net.runelite.api.MenuAction.RUNELITE_OVERLAY;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.ComponentOrientation;

@Slf4j
public class NotificationPanelOverlay extends OverlayPanel
{
	static final String CLEAR_ALL = "Clear";
	static final int GAP = 6;
	static final Color TRANSPARENT = new Color(0, 0, 0, 0);
	static final private Dimension DEFAULT_SIZE = new Dimension(250, 60);
	@Getter
	@Setter
	static ConcurrentLinkedQueue<Notification> notificationQueue =
		new ConcurrentLinkedQueue<>();
	@Setter
	static boolean shouldUpdateBoxes;
	static private Dimension preferredSize = DEFAULT_SIZE;
	final private NotificationPanelConfig config;
	private Client client;
	private int lastDeletedTick = -1;
	private Point lastDeletedPos = new Point(-50,-50);

	@Inject
	private NotificationPanelOverlay(NotificationPanelConfig config, Client client)
	{
		this.config = config;
		this.client = client;

		setResizable(true);
		setPosition(OverlayPosition.TOP_LEFT);
		setPriority(OverlayPriority.LOW);
		setClearChildren(false);

		panelComponent.setWrap(false);
		panelComponent.setBorder(new Rectangle(0, 0, 0, 0));
		panelComponent.setOrientation(ComponentOrientation.VERTICAL);
		panelComponent.setGap(new Point(0, GAP));
		panelComponent.setBackgroundColor(TRANSPARENT);


		getMenuEntries().add(new OverlayMenuEntry(RUNELITE_OVERLAY, CLEAR_ALL,
			"Notification " + "panel"));
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (notificationQueue.isEmpty())
		{
			return null;
		}

		graphics.setFont(config.fontType().getFont());

		final Dimension newPreferredSize = getPreferredSize();

		if (newPreferredSize == null)
		{
			preferredSize = DEFAULT_SIZE;
			setPreferredSize(preferredSize);
			shouldUpdateBoxes = true;
		}
		// if we just compare the Dimension objects, they will always be different
		// so just look at the widths. we can't manually control the height anyway, so ignore it.
		else if (newPreferredSize.width != preferredSize.width)
		{
			preferredSize = newPreferredSize;
			shouldUpdateBoxes = true;
		}

		// only rebuild the panel when necessary
		if (shouldUpdateBoxes)
		{
			while (notificationQueue.size() > config.numToShow())
			{
				notificationQueue.poll();
			}

			notificationQueue.forEach(s -> s.makeBox(graphics, preferredSize));

			panelComponent.getChildren().clear();
			notificationQueue.forEach(s -> panelComponent.getChildren().add(s.getBox()));
			updatePanelSize();

			shouldUpdateBoxes = false;
		}

		return super.render(graphics);
	}

	@Override
	public void onMouseOver(){
		if (!config.dismissOnHover())
		{
			return;
		}

		// mouse pos relative to this overlay
		Point mousePos = new Point(
			(int) (client.getMouseCanvasPosition().getX() - this.getBounds().getX()),
			(int) (client.getMouseCanvasPosition().getY() - this.getBounds().getY()));

		if (mousePositionDifference(lastDeletedPos, mousePos) || lastDeletedTick < client.getTickCount() - 1){
			Notification toRemove = null;

			for (Notification notification : notificationQueue){
				if (notification.getBox().getBounds().contains(mousePos)){
					toRemove = notification;
					lastDeletedTick = client.getTickCount();
					lastDeletedPos = mousePos;
					break;
				}
			}

			if (toRemove != null){
				// concurrent access error? does this happen often enough to do ClientThread.invokeLater or something
				notificationQueue.remove(toRemove);
				shouldUpdateBoxes = true;
			}
		}

	}

	boolean mousePositionDifference(Point a, Point b){
		return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY()) > 30;
	}

	void updatePanelSize()
	{
		int width = 2;
		int minWidth = 500;
		int height = 0;

		for (Notification notification : notificationQueue)
		{
			width = Math.max(width, notification.getWidth());
			minWidth = Math.min(minWidth, notification.getMaxWordWidth());
			height = Math.max(height, notification.getHeight());
		}

		setPreferredSize(new Dimension(width, height));
		setMinimumSize(minWidth);
	}

}
