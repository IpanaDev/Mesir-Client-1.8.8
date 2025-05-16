package ipana.utils.gamepad;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.Event;
import net.java.games.input.EventQueue;

import java.util.HashMap;

public class GamePad {
    private String name;
    private Controller input;
    private HashMap<Component.Identifier,Float> inputMap = new HashMap<>();
    private HashMap<Float,PovDirection> povMap = new HashMap<>();
    private EventQueue queue;
    private Event event;

    public GamePad(String name, Controller input) {
        this.name = name;
        this.input = input;
        queue = input.getEventQueue();
        event = new Event();
        for (PovDirection pov : PovDirection.VALUES) {
            povMap.put(pov.ordinal()*0.125f,pov);
        }
    }

    public void update() {
        input.poll();
        queue.getNextEvent(event);

        Component component = event.getComponent();

        if (component != null) {
            if (!inputMap.containsKey(component.getIdentifier())) {
                inputMap.put(component.getIdentifier(), component.getPollData());
            } else {
                inputMap.replace(component.getIdentifier(), component.getPollData());
            }
        }
    }

    public float[] leftStick() {
        float x = round(getInput(Component.Identifier.Axis.X));
        float y = round(getInput(Component.Identifier.Axis.Y));
        return new float[]{x,y};
    }
    public float[] rightStick() {
        float x = round(getInput(Component.Identifier.Axis.RX));
        float y = round(getInput(Component.Identifier.Axis.RY));
        return new float[]{x,y};
    }

    public boolean buttonA() {
        float f = ((int)getInput(Component.Identifier.Button._0));
        return f == 1;
    }
    public boolean buttonB() {
        float f = ((int)getInput(Component.Identifier.Button._1));
        return f == 1;
    }
    public boolean buttonX() {
        float f = ((int)getInput(Component.Identifier.Button._2));
        return f == 1;
    }
    public boolean buttonY() {
        float f = ((int)getInput(Component.Identifier.Button._3));
        return f == 1;
    }
    public boolean leftThumb() {
        float f = ((int)getInput(Component.Identifier.Button._4));
        return f == 1;
    }
    public boolean rightThumb() {
        float f = ((int)getInput(Component.Identifier.Button._5));
        return f == 1;
    }
    public float leftTrigger() {
        return Math.min(0,-round(getInput(Component.Identifier.Axis.Z)));
    }
    public float rightTrigger() {
        return Math.max(0,-round(getInput(Component.Identifier.Axis.Z)));
    }
    public PovDirection pov() {
        float input = getInput(Component.Identifier.Axis.POV);
        return povMap.get(input);
    }
    private float round(float value) {
        if (value > 0 && value < 0.004) {
            return 0;
        } else if (value < 0 && value > -0.004) {
            return 0;
        }
        return value;
    }

    private float getInput(Component.Identifier identifier) {
        Float raw = inputMap.get(identifier);
        float input = 0;
        if (raw != null) {
            input = raw;
        }
        return input;
    }

    public String getName() {
        return name;
    }

    public enum PovDirection {
        NULL,UP_LEFT,UP,UP_RIGHT,RIGHT,DOWN_RIGHT,DOWN,DOWN_LEFT,LEFT;
        public static final PovDirection[] VALUES = values();
    }
}
