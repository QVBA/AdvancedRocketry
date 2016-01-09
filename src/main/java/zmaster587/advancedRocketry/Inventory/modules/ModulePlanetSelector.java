package zmaster587.advancedRocketry.Inventory.modules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import zmaster587.advancedRocketry.Inventory.GuiModular;
import zmaster587.advancedRocketry.Inventory.TextureResources;
import zmaster587.advancedRocketry.api.dimension.DimensionManager;
import zmaster587.advancedRocketry.api.dimension.DimensionProperties;
import zmaster587.advancedRocketry.api.dimension.solar.StellarBody;
import zmaster587.libVulpes.render.RenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.util.ResourceLocation;

public class ModulePlanetSelector extends ModuleContainerPan implements IButtonInventory {

	private static final int size = 2000;
	private static final int starIdOffset = 10000;
	ISelectionNotify hostTile;
	private int currentSystem, selectedSystem;
	private boolean currentSystemChanged = false;
	private List<ModuleButton> planetList;

	private HashMap<Integer, PlanetRenderProperties> renderPropertiesMap;
	PlanetRenderProperties currentlySelectedPlanet;

	public ModulePlanetSelector(int planetId, ResourceLocation backdrop, ISelectionNotify tile) {
		super(0, 0, null, null, backdrop, 0, 0, 0, 0, size,size);

		hostTile = tile;
		int center = size/2;

		planetList = new ArrayList<ModuleButton>();
		moduleList = new ArrayList<ModuleBase>();
		staticModuleList = new ArrayList<ModuleBase>();
		renderPropertiesMap = new HashMap<Integer, PlanetRenderProperties>();
		currentlySelectedPlanet = new PlanetRenderProperties();
		currentSystem = starIdOffset;
		selectedSystem = -1;

		staticModuleList.add(new ModuleButton(0, 0, -1, "<< Up", this, TextureResources.buttonBuild));
		staticModuleList.add(new ModuleButton(0, 18, -2, "Select", this, TextureResources.buttonBuild));

		ModuleDualProgressBar progressBar;
		staticModuleList.add(progressBar = new ModuleDualProgressBar(100, 0, 0, TextureResources.atmIndicator, (IProgressBar)tile, "%b -> %a Earth's atmospheric pressure"));
		progressBar.setTooltipValueMultiplier(.02f);

		staticModuleList.add(progressBar = new ModuleDualProgressBar(200, 0, 2, TextureResources.massIndicator, (IProgressBar)tile, "%b -> %a Earth's mass"));
		progressBar.setTooltipValueMultiplier(.02f);

		staticModuleList.add(progressBar = new ModuleDualProgressBar(300, 0, 1, TextureResources.distanceIndicator, (IProgressBar)tile, "%b -> %a Relative Distance units"));
		progressBar.setTooltipValueMultiplier(.02f);

		//renderPlanetarySystem(properties, center, center, 3f);
		if(FMLCommonHandler.instance().getSide().isClient())
			renderStarSystem(DimensionManager.getInstance().getStar(0), center, center, 5f, 1f);
	}

	@Override
	public void onScroll(int dwheel) {
	}
	
	public int getSelectedSystem() {
		return selectedSystem;
	}

	public void setSelectedSystem(int id) {
		selectedSystem = id;
	}

	@SideOnly(Side.CLIENT)
	private void renderStarSystem(StellarBody star, int posX, int posY, float distanceZoomMultiplier, float planetSizeMultiplier) {

		int displaySize = (int)(planetSizeMultiplier*star.getDisplayRadius());

		int offsetX = posX - displaySize/2; 
		int offsetY = posY - displaySize/2; 

		ModuleButton button;
		planetList.add(button = new ModuleButton(offsetX, offsetY, star.getId() + starIdOffset, "", this, new ResourceLocation[] { TextureResources.locationSunPng }, displaySize, displaySize));

		button.setSound("buttonBlipA");
		button.setBGColor(star.getColorRGB8());

		renderPropertiesMap.put(star.getId() + starIdOffset, new PlanetRenderProperties(displaySize, offsetX, offsetY));
		//prevMultiplier *= 0.25f;

		for(DimensionProperties properties : star.getPlanets()) {
			if(!properties.isMoon())
			renderPlanets(properties, offsetX + displaySize/2, offsetY + displaySize/2, displaySize, distanceZoomMultiplier,planetSizeMultiplier);
		}

		moduleList.addAll(planetList);
	}

	@SideOnly(Side.CLIENT)
	private void renderPlanetarySystem(DimensionProperties planet, int posX, int posY, float distanceZoomMultiplier, float planetSizeMultiplier) {

		int displaySize = (int)(planetSizeMultiplier*planet.gravitationalMultiplier/.02f);

		int offsetX = posX - displaySize/2; 
		int offsetY = posY - displaySize/2; 

		ModuleButton button;
		planetList.add(button = new ModuleButton(offsetX, offsetY, planet.getId(), "", this, new ResourceLocation[] { DimensionProperties.PlanetIcons.UNKNOWN.getResource() }, planet.getName(), displaySize, displaySize));
		button.setSound("buttonBlipA");

		renderPropertiesMap.put(planet.getId(), new PlanetRenderProperties(displaySize, offsetX, offsetY));

		//prevMultiplier *= 0.25f;

		for(Integer childId : planet.getChildPlanets()) {
			DimensionProperties properties = DimensionManager.getInstance().getDimensionProperties(childId);
			renderPlanets(properties, offsetX + displaySize/2, offsetY + displaySize/2, displaySize, distanceZoomMultiplier, planetSizeMultiplier);
		}

		moduleList.addAll(planetList);
	}

	@SideOnly(Side.CLIENT)
	private void renderPlanets(DimensionProperties planet, int parentOffsetX, int parentOffsetY, int parentRadius, float distanceMultiplier, float planetSizeMultiplier) {

		int displaySize = (int)(planetSizeMultiplier*planet.gravitationalMultiplier/.02f);

		int offsetX = parentOffsetX + (int)(Math.cos(planet.orbitTheta)*((planet.orbitalDist*distanceMultiplier) + parentRadius)) - displaySize/2;
		int offsetY = parentOffsetY + (int)(Math.sin(planet.orbitTheta)*((planet.orbitalDist*distanceMultiplier) + parentRadius)) - displaySize/2;

		ModuleButton button;

		planetList.add(button = new ModuleButton(offsetX, offsetY, planet.getId(), "", this, new ResourceLocation[] { DimensionProperties.PlanetIcons.UNKNOWN.getResource() }, planet.getName() + "\nMoons: " + planet.getChildPlanets().size(), displaySize, displaySize));
		button.setSound("buttonBlipA");

		renderPropertiesMap.put(planet.getId(), new PlanetRenderProperties(displaySize, offsetX, offsetY));
	}

	
	@SideOnly(Side.CLIENT)
	public void setPlanetAsKnown(int id) {
		for(ModuleBase module : moduleList) {
			if(module instanceof ModuleButton && ((ModuleButton)module).buttonId == id) {
				((ModuleButton)module).setImage( new ResourceLocation[] {DimensionManager.getInstance().getDimensionProperties(id).getPlanetIcon()});
			}
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public List<GuiButton> addButtons(int x, int y) {

		this.screenSizeX = Minecraft.getMinecraft().displayWidth;
		this.screenSizeY = Minecraft.getMinecraft().displayHeight;

		setOffset2(internalOffsetX - Minecraft.getMinecraft().displayWidth/4, internalOffsetY - Minecraft.getMinecraft().displayHeight /4);

		return super.addButtons(x, y);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onMouseClicked(GuiModular gui, int x, int y, int button) {

		super.onMouseClicked(gui, x, y, button);

		//CME workaround
		if(currentSystemChanged) {
			currentPosX = 0;
			currentPosY = 0;
			this.moduleList.removeAll(planetList);
			planetList.clear();
			if(currentSystem < starIdOffset) {
				DimensionProperties properties = DimensionManager.getInstance().getDimensionProperties(currentSystem);
				renderPlanetarySystem(properties, size/2, size/2, 1f,3f*properties.getPathLengthToStar());
			}
			else
				renderStarSystem(DimensionManager.getInstance().getStar(currentSystem - starIdOffset), size/2, size/2, 5f, 1f);
			addButtons(0, 0);

			selectedSystem = -1;

			currentSystemChanged = false;

			hostTile.onSystemFocusChanged(this);

		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderBackground(GuiContainer gui, int x, int y, int mouseX,
			int mouseY, FontRenderer font) {
		super.renderBackground(gui, x, y, mouseX, mouseY, font);


		int center = size/2;
		int numSegments = 50;

		float theta = (float) (2 * Math.PI / (float)(numSegments));
		float cos = (float) Math.cos(theta);
		float sin = (float) Math.sin(theta);
		//Render orbits
		for(int ii = 1; ii < planetList.size(); ii++) {
			
			ModuleButton base = planetList.get(ii);
			
			int radius = (int) Math.sqrt(Math.pow(base.offsetX + 40 - center - currentPosX,2) + Math.pow(base.offsetY + 40 - center - currentPosY,2));
			float x2 = radius;
			float y2 = 0;
			float t;
			GL11.glPushMatrix();
			GL11.glTranslatef(center + currentPosX, center + currentPosY, 0);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GL11.glColor4f(0.8f, .8f, 1f, .2f);
			GL11.glEnable(GL11.GL_LINE_STIPPLE);
			GL11.glLineStipple(5, (short)0x5555);
			
			Tessellator.instance.startDrawing(GL11.GL_LINE_LOOP);
			for(int i = 0; i < numSegments; i++)	{
				Tessellator.instance.addVertex(x2, y2, 0);
				t = x2;
				x2 = cos*x2 - sin*y2;
				y2 = sin*t + cos*y2;
			}
			Tessellator.instance.draw();
			//Reset GL info
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glDisable(GL11.GL_LINE_STIPPLE);
			GL11.glColor4f(1f, 1f, 1f, 1f);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glPopMatrix();

		}

		//Render Selection
		if(selectedSystem != -1) {

			Minecraft.getMinecraft().getTextureManager().bindTexture(TextureResources.selectionCircle);
			GL11.glPushMatrix();
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			float radius = currentlySelectedPlanet.radius/2;
			GL11.glTranslatef(currentlySelectedPlanet.posX + currentPosX + radius, currentlySelectedPlanet.posY  + currentPosY + radius, 0);

			double progress = System.currentTimeMillis() % 20000 / 50f;
			
			GL11.glPushMatrix();
			GL11.glRotated(progress, 0, 0, 1);
			Tessellator.instance.startDrawingQuads();
			RenderHelper.renderNorthFaceWithUV(Tessellator.instance, 1, -radius, -radius, radius, radius, 0, 1, 0, 1);
			Tessellator.instance.draw();
			GL11.glPopMatrix();

			GL11.glPushMatrix();
			//GL11.glRotatef(-Minecraft.getMinecraft().theWorld.getTotalWorldTime(), 0, 0, 1);
			radius *= (1.2 + 0.1*Math.sin(progress/10f));
			Tessellator.instance.startDrawingQuads();
			RenderHelper.renderNorthFaceWithUV(Tessellator.instance, 1, -radius, -radius, radius, radius, 0, 1, 0, 1);
			Tessellator.instance.draw();
			GL11.glPopMatrix();
			GL11.glDisable(GL11.GL_BLEND);

			GL11.glPopMatrix();
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onInventoryButtonPressed(int buttonId) {

		if(buttonId == -1) {
			DimensionProperties properties =  DimensionManager.getInstance().getDimensionProperties(currentSystem);

			if(properties.isMoon())
				currentSystem = properties.getParentPlanet();
			else
				currentSystem = properties.getStar().getId() + starIdOffset;

			currentSystemChanged=true;
			selectedSystem = -1;
		}
		else if(buttonId == -2) {
			if(selectedSystem < starIdOffset) {
				hostTile.onSelectionConfirmed(this);
				Minecraft.getMinecraft().thePlayer.closeScreen();
			}
		}
		else {
			if(selectedSystem == buttonId) {
				currentSystem = buttonId;
				currentSystemChanged=true;
				selectedSystem = -1;
			}
			else {
				selectedSystem = buttonId;
				currentlySelectedPlanet = renderPropertiesMap.get(buttonId);

				hostTile.onSelected(this);
			}
		}
	}

	@Override
	protected boolean needsUpdate(int localId) {
		for(ModuleBase module : staticModuleList) {
			if(localId >= 0 && localId < module.numberOfChangesToSend())
				return module.needsUpdate(localId);

			localId -= module.numberOfChangesToSend();
		}
		return false;
	}

	@Override
	public void sendChanges(Container container, ICrafting crafter,
			int variableId, int localId) {
		for(ModuleBase module : staticModuleList) {
			if(localId >= 0 && localId < module.numberOfChangesToSend()) {
				module.sendChanges(container, crafter, variableId, localId);
				return;
			}

			localId -= module.numberOfChangesToSend();
		}
	}

	@Override
	public void onChangeRecieved(int slot, int value) {
		for(ModuleBase module : staticModuleList) {
			if(slot >= 0 && slot < module.numberOfChangesToSend()) {
				module.onChangeRecieved(slot, value);
				return;
			}

			slot -= module.numberOfChangesToSend();
		}
	}

	@Override
	public int numberOfChangesToSend() {
		int numChanges = 0;
		for(ModuleBase module : staticModuleList) {
			numChanges += module.numberOfChangesToSend();
		}

		return numChanges;
	}

	//Closest thing i can get to a struct :/
	private class PlanetRenderProperties {
		int radius;
		int posX;
		int posY;

		public PlanetRenderProperties() {}

		public PlanetRenderProperties(int radius, int posX, int posY) {
			this.radius = radius;
			this.posX = posX;
			this.posY = posY;
		}
	}
}
