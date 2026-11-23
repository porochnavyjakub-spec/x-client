package dev.xclient.modules;

import dev.xclient.core.*;
import dev.xclient.core.Module;

public class Sprint extends Module {
    public Sprint() { super("Sprint", "Always sprint while moving forward", Category.MOVEMENT); }

    @Override
    public void onTick() {
        var p = mc.player;
        if (p == null) return;
        if (mc.options.keyUp.isDown() && !p.isShiftKeyDown() && !p.horizontalCollision
                && p.getFoodData().getFoodLevel() > 6) {
            p.setSprinting(true);
        }
    }
}
