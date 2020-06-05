package xyz.wagyourtail.freecam;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import xyz.wagyourtail.freecam.event.KeyEvent;

public class Freecam implements ClientModInitializer {
	public static final String MOD_ID = "freecam";
	public static MinecraftClient mc;
	private static FabricKeyBinding keyBinding;
	public static CameraEntity fakePlayer;
	public static float speed;
//	private static float upV;
//	private static float forwardV;
//	private static float sideV;
	private static int savedPerspective;
	public static boolean isFreecam = false;
//	
//	private static boolean forwardKey;
//	private static boolean backwardKey;
//	private static boolean leftKey;
//	private static boolean rightKey;
//	private static boolean upKey;
//	private static boolean downKey;
//	
	
	public void enableFreecam() {
		if (mc.player != null) {
			//doing hunger like this (setting same hunger manager) might not be the best way but idk...
			fakePlayer = new CameraEntity(mc.world, mc.player.getGameProfile(), mc.player.getHungerManager());
			fakePlayer.copyPositionAndRotation(mc.player);
			fakePlayer.setHeadYaw(mc.player.headYaw);
			fakePlayer.spawn();
			savedPerspective = mc.options.perspective;
			mc.options.perspective = 0;
			mc.setCameraEntity(fakePlayer);
		}
    }
    
    public void disableFreecam() {
    	mc.options.perspective = savedPerspective;
		mc.setCameraEntity(mc.player);
		if (fakePlayer != null) fakePlayer.despawn();
		fakePlayer = null;
    }
    
    public void setSpeed(float set) {
    	speed = set;
    }
	
	
	@Override
	public void onInitializeClient() {
		// TODO Auto-generated method stub
		mc = MinecraftClient.getInstance();
		setSpeed(.25F); //default speed
		
		keyBinding = FabricKeyBinding.Builder.create(new Identifier("freecam", "toggle"), InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_U, "Freecam").build();
        KeyBindingRegistry.INSTANCE.addCategory("Freecam");
        KeyBindingRegistry.INSTANCE.register(keyBinding);
        
        
        ClientTickCallback.EVENT.register(e -> {
        	
    		if (mc.player == null && isFreecam  == true) {
    			mc.options.perspective = savedPerspective;
    			isFreecam = false;
    			fakePlayer = null;
    		}
    		
    		if (isFreecam && fakePlayer == null) {
    			disableFreecam();
    		}
    		
        	if (fakePlayer != null) {
        		fakePlayer.setHealth(mc.player.getHealth());
    			if (mc.options.perspective != 0) mc.options.perspective = 0;
    			
    			//the instanceof allows baritone to keep working as it replaces the mc.player.input
    			if (mc.player != null && mc.player.input instanceof KeyboardInput) mc.player.input = new DummyInput();
    			fakePlayer.tickMovement();
        	}
        });
        
        KeyEvent.EVENT.register((window, key, scancode, action, mods) -> {
            
            if (keyBinding.matchesKey(key, scancode) && action == 1 && mc.currentScreen == null) {
            	isFreecam = !isFreecam; 
            	if (isFreecam) {
        			this.enableFreecam();
            	} else {
            		this.disableFreecam();
            	}
            }
            
			return ActionResult.PASS;
        });
        
	}

	
}
