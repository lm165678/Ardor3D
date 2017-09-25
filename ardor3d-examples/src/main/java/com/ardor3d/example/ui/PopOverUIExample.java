/**
 * Copyright (c) 2008-2017 Ardor Labs, Inc.
 *
 * This file is part of Ardor3D.
 *
 * Ardor3D is free software: you can redistribute it and/or modify it
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://www.ardor3d.com/LICENSE>.
 */

package com.ardor3d.example.ui;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.example.ExampleBase;
import com.ardor3d.example.Purpose;
import com.ardor3d.extension.ui.UIButton;
import com.ardor3d.extension.ui.UIComponent;
import com.ardor3d.extension.ui.UIHud;
import com.ardor3d.extension.ui.UIMenuItem;
import com.ardor3d.extension.ui.UIPieMenu;
import com.ardor3d.extension.ui.UIPieMenuItem;
import com.ardor3d.extension.ui.UIPopupMenu;
import com.ardor3d.extension.ui.event.ActionEvent;
import com.ardor3d.extension.ui.event.ActionListener;
import com.ardor3d.extension.ui.util.Insets;
import com.ardor3d.image.Texture;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.MathUtils;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.MaterialState.ColorMaterial;
import com.ardor3d.renderer.state.RenderState.StateType;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.controller.SpatialController;
import com.ardor3d.scenegraph.shape.Box;
import com.ardor3d.util.ReadOnlyTimer;
import com.ardor3d.util.TextureManager;

/**
 * Illustrates the use of Popup and Pie menus.
 */
@Purpose(htmlDescriptionKey = "com.ardor3d.example.ui.PopOverUIExample", //
thumbnailPath = "com/ardor3d/example/media/thumbnails/ui_PopOverUIExample.jpg", //
maxHeapMemory = 64)
public class PopOverUIExample extends ExampleBase implements ActionListener {
    private static final String[] COLORS = new String[] { "Red", "White", "Blue", "Black" };
    private static final String[] SPINS = new String[] { "None", "Around X", "Around Y", "Around Z" };
    private static final String[] TEXS = new String[] { "None", "Logo", "Ball", "Clock" };

    UIHud hud;
    private Box box;

    public static void main(final String[] args) {
        start(PopOverUIExample.class);
    }

    @Override
    protected void initExample() {
        _canvas.setTitle("PopOver UI Example");

        UIComponent.setUseTransparency(true);

        // Add a spinning 3D box to show behind UI.
        box = new Box("Box", new Vector3(0, 0, 0), 5, 5, 5);
        box.setModelBound(new BoundingBox());
        box.setTranslation(new Vector3(0, 0, -15));
        _root.attachChild(box);

        final MaterialState ms = new MaterialState();
        ms.setColorMaterial(ColorMaterial.Diffuse);
        box.setRenderState(ms);

        setTexture("Logo");
        setSpin("Around Y");

        hud = new UIHud();
        hud.setupInput(_canvas, _physicalLayer, _logicalLayer);
        hud.setMouseManager(_mouseManager);
        final Camera cam = _canvas.getCanvasRenderer().getCamera();

        final UIButton dropButton = new UIButton("Drop Menu");
        dropButton.setPadding(new Insets(6, 15, 6, 15));
        dropButton.setHudXY(cam.getWidth() / 10 - dropButton.getLocalComponentWidth() / 2,
                cam.getHeight() - dropButton.getLocalComponentHeight() - 5);
        dropButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                showPopupMenu(dropButton.getHudX(), dropButton.getHudY());
            }
        });
        hud.add(dropButton);

        final UIButton pieButton = new UIButton("Pie Menu");
        pieButton.setPadding(new Insets(6, 15, 6, 15));
        pieButton.setHudXY(9 * cam.getWidth() / 10 - pieButton.getLocalComponentWidth() / 2, cam.getHeight()
                - pieButton.getLocalComponentHeight() - 5);
        pieButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                showPieMenu(cam.getWidth() / 2, cam.getHeight() / 2);
            }
        });
        hud.add(pieButton);
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
        final UIButton src = (UIButton) event.getSource();
        final String command = src.getActionCommand();
        switch (command) {
            case "Color":
                setColor(src.getText());
                return;
            case "Spin":
                setSpin(src.getText());
                return;
            case "Texture":
                setTexture(src.getText());
                return;
        }
    }

    protected void showPopupMenu(final int hudX, final int hudY) {
        final UIPopupMenu menu = new UIPopupMenu();
        final int minWidth = 120;

        final UIPopupMenu colorMenu = new UIPopupMenu();
        colorMenu.setMinimumContentSize(minWidth, 5);
        menu.addItem(new UIMenuItem("Set Color...", null, colorMenu));
        AddMenuItems(colorMenu, "Color", false, COLORS);

        final UIPopupMenu spinMenu = new UIPopupMenu();
        spinMenu.setMinimumContentSize(minWidth, 5);
        menu.addItem(new UIMenuItem("Set Spin...", null, spinMenu));
        AddMenuItems(spinMenu, "Spin", false, SPINS);

        final UIPopupMenu texMenu = new UIPopupMenu();
        texMenu.setMinimumContentSize(minWidth, 5);
        menu.addItem(new UIMenuItem("Set Texture...", null, texMenu));
        AddMenuItems(texMenu, "Texture", false, TEXS);

        menu.updateMinimumSizeFromContents();
        menu.layout();

        hud.closePopupMenus();

        hud.showSubPopupMenu(menu);
        menu.showAt(hudX, hudY);
    }

    protected void showPieMenu(final int hudX, final int hudY) {
        final UIPieMenu menu = new UIPieMenu(hud, 70, 200);

        final UIPieMenu colorMenu = new UIPieMenu(hud);
        menu.addItem(new UIPieMenuItem("Set Color...", null, colorMenu, 100));
        AddMenuItems(colorMenu, "Color", true, COLORS);

        final UIPieMenu spinMenu = new UIPieMenu(hud);
        menu.addItem(new UIPieMenuItem("Set Spin...", null, spinMenu, 100));
        AddMenuItems(spinMenu, "Spin", true, SPINS);

        final UIPieMenu texMenu = new UIPieMenu(hud);
        menu.addItem(new UIPieMenuItem("Set Texture...", null, texMenu, 100));
        AddMenuItems(texMenu, "Texture", true, TEXS);

        menu.setCenterItem(new UIPieMenuItem("Cancel", null, true, null));

        menu.updateMinimumSizeFromContents();
        menu.layout();

        hud.closePopupMenus();

        hud.showSubPopupMenu(menu);
        menu.showAt(hudX, hudY);
        _mouseManager.setPosition(hudX, hudY);
        if (menu.getCenterItem() != null) {
            menu.getCenterItem().mouseEntered(hudX, hudY, null);
        }

    }

    private void AddMenuItems(final UIPopupMenu parent, final String actionCommand, final boolean pie,
            final String[] colors) {
        for (final String color : colors) {
            final UIMenuItem item = pie ? new UIPieMenuItem(color, null, true, this) : new UIMenuItem(color, null,
                    true, this);
            item.setActionCommand(actionCommand);
            parent.addItem(item);
        }
    }

    private void setColor(final String text) {
        switch (text) {
            case "Red":
                box.setDefaultColor(ColorRGBA.RED);
                break;
            case "Blue":
                box.setDefaultColor(ColorRGBA.BLUE);
                break;
            case "Black":
                box.setDefaultColor(ColorRGBA.BLACK);
                break;
            default:
            case "White":
                box.setDefaultColor(ColorRGBA.WHITE);
                break;
        }
    }

    private void setSpin(final String text) {
        box.clearControllers();
        final ReadOnlyVector3 axis;
        switch (text) {
            case "None":
                return;
            case "Around X":
                axis = Vector3.UNIT_X;
                break;
            case "Around Y":
                axis = Vector3.UNIT_Y;
                break;
            default:
            case "Around Z":
                axis = Vector3.UNIT_Z;
                break;
        }
        box.addController(new SpatialController<Box>() {
            private final Matrix3 rotate = new Matrix3();
            private double angle = 0;

            public void update(final double time, final Box caller) {
                angle += time * 50;
                angle %= 360;
                rotate.fromAngleNormalAxis(angle * MathUtils.DEG_TO_RAD, axis);
                caller.setRotation(rotate);
            }
        });
    }

    private void setTexture(final String text) {
        // Add a texture to the box.
        final TextureState ts = new TextureState();

        String imageFile;
        switch (text) {
            case "None":
                box.clearRenderState(StateType.Texture);
                box.updateWorldRenderStates(true);
                return;
            case "Ball":
                imageFile = "images/ball.png";
                break;
            case "Clock":
                imageFile = "images/clock.png";
                break;
            case "Logo":
            default:
                imageFile = "images/ardor3d_white_256.jpg";
                break;
        }

        final Texture tex = TextureManager.load(imageFile, Texture.MinificationFilter.Trilinear, true);
        ts.setTexture(tex);
        box.setRenderState(ts);
        box.updateWorldRenderStates(true);
    }

    @Override
    protected void updateLogicalLayer(final ReadOnlyTimer timer) {
        hud.getLogicalLayer().checkTriggers(timer.getTimePerFrame());
    }

    @Override
    protected void renderExample(final Renderer renderer) {
        super.renderExample(renderer);
        renderer.renderBuckets();
        renderer.draw(hud);
    }

    @Override
    protected void updateExample(final ReadOnlyTimer timer) {
        hud.updateGeometricState(timer.getTimePerFrame());
    }

}