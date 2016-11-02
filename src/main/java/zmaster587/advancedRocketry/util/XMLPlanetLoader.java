package zmaster587.advancedRocketry.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import zmaster587.advancedRocketry.AdvancedRocketry;
import zmaster587.advancedRocketry.api.dimension.solar.StellarBody;
import zmaster587.advancedRocketry.dimension.DimensionManager;
import zmaster587.advancedRocketry.dimension.DimensionProperties;

public class XMLPlanetLoader {

	Document doc;
	NodeList currentList;
	int currentNodeIndex;
	int starId;
	int offset;

	HashMap<StellarBody, Integer> maxPlanetNumber = new HashMap<StellarBody, Integer>();
	HashMap<StellarBody, Integer> maxGasPlanetNumber = new HashMap<StellarBody, Integer>();

	public boolean loadFile(File xmlFile) throws IOException {
		DocumentBuilder docBuilder;
		doc = null;
		try {
			docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			return false;
		}

		try {
			doc = docBuilder.parse(xmlFile);
		} catch (SAXException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	public XMLPlanetLoader() {
		doc = null;
		currentNodeIndex = -1;
		starId=0;
	}

	public boolean isValid() {
		return doc != null;
	}

	public int getMaxNumPlanets(StellarBody body) {
		return maxPlanetNumber.get(body);
	}


	public int getMaxNumGasGiants(StellarBody body) {
		return maxGasPlanetNumber.get(body);
	}

	private List<DimensionProperties> readPlanetFromNode(Node planetNode, StellarBody star) {
		List<DimensionProperties> list = new ArrayList<DimensionProperties>();
		Node planetPropertyNode = planetNode.getFirstChild();


		DimensionProperties properties = new DimensionProperties(DimensionManager.getInstance().getNextFreeDim(offset));
		list.add(properties);
		offset++;//Increment for dealing with child planets


		//Set name for dimension if exists
		if(planetNode.hasAttributes()) {
			Node nameNode = planetNode.getAttributes().getNamedItem("name");
			if(nameNode != null && !nameNode.getNodeValue().isEmpty()) {
				properties.setName(nameNode.getNodeValue());
			}
			
			nameNode = planetNode.getAttributes().getNamedItem("DIMID");
			if(nameNode != null && !nameNode.getNodeValue().isEmpty()) {
				try {
					properties.setId(Integer.parseInt(nameNode.getTextContent()));
					//We're not using the offset so decrement to prepare for next planet
					offset--;
				} catch (NumberFormatException e) {
					AdvancedRocketry.logger.warning("Invalid DIMID specified"); //TODO: more detailed error msg
				}
			}
			
			nameNode = planetNode.getAttributes().getNamedItem("dimMapping");
			if(nameNode != null) {
				properties.isNativeDimension = false;
			}
		}

		while(planetPropertyNode != null) {
			if(planetPropertyNode.getNodeName().equalsIgnoreCase("fogcolor")) {
				String[] colors = planetPropertyNode.getTextContent().split(",");
				if(colors.length >= 3) {
					float rgb[] = new float[3];

					try {
						for(int j = 0; j < 3; j++)
							rgb[j] = Float.parseFloat(colors[j]);
						properties.fogColor = rgb;
					} catch (NumberFormatException e) {
						AdvancedRocketry.logger.warning("Invalid fog color specified"); //TODO: more detailed error msg
					}
				}
				else
					AdvancedRocketry.logger.warning("Invalid number of floats specified for fog color (Required 3, comma sperated)"); //TODO: more detailed error msg
			}
			else if(planetPropertyNode.getNodeName().equalsIgnoreCase("skycolor")) {
				String[] colors = planetPropertyNode.getTextContent().split(",");
				if(colors.length >= 3) {
					float rgb[] = new float[3];

					try {
						for(int j = 0; j < 3; j++)
							rgb[j] = Float.parseFloat(colors[j]);
						properties.skyColor = rgb;
					} catch (NumberFormatException e) {
						AdvancedRocketry.logger.warning("Invalid sky color specified"); //TODO: more detailed error msg
					}
				}
				else
					AdvancedRocketry.logger.warning("Invalid number of floats specified for sky color (Required 3, comma sperated)"); //TODO: more detailed error msg
			}
			else if(planetPropertyNode.getNodeName().equalsIgnoreCase("atmosphereDensity")) {

				try {
					properties.setAtmosphereDensityDirect(Math.min(Math.max(Integer.parseInt(planetPropertyNode.getTextContent()), DimensionProperties.MIN_ATM_PRESSURE), DimensionProperties.MAX_ATM_PRESSURE));
				} catch (NumberFormatException e) {
					AdvancedRocketry.logger.warning("Invalid atmosphereDensity specified"); //TODO: more detailed error msg
				}
			}
			else if(planetPropertyNode.getNodeName().equalsIgnoreCase("gravitationalmultiplier")) {

				try {
					properties.gravitationalMultiplier = Math.min(Math.max(Integer.parseInt(planetPropertyNode.getTextContent()), DimensionProperties.MIN_GRAVITY), DimensionProperties.MAX_GRAVITY)/100f;
				} catch (NumberFormatException e) {
					AdvancedRocketry.logger.warning("Invalid gravitationalMultiplier specified"); //TODO: more detailed error msg
				}
			}
			else if(planetPropertyNode.getNodeName().equalsIgnoreCase("orbitaldistance")) {

				try {
					properties.orbitalDist = Math.min(Math.max(Integer.parseInt(planetPropertyNode.getTextContent()), DimensionProperties.MIN_DISTANCE), DimensionProperties.MAX_DISTANCE);
				} catch (NumberFormatException e) {
					AdvancedRocketry.logger.warning("Invalid orbitalDist specified"); //TODO: more detailed error msg
				}
			}
			else if(planetPropertyNode.getNodeName().equalsIgnoreCase("orbitaltheta")) {

				try {
					properties.orbitTheta = (Integer.parseInt(planetPropertyNode.getTextContent()) % 360) * 2/Math.PI;
				} catch (NumberFormatException e) {
					AdvancedRocketry.logger.warning("Invalid orbitalTheta specified"); //TODO: more detailed error msg
				}
			}
			else if(planetPropertyNode.getNodeName().equalsIgnoreCase("rotationalperiod")) {
				try {
					int rotationalPeriod =  Integer.parseInt(planetPropertyNode.getTextContent());
					if(properties.rotationalPeriod > 0)
						properties.rotationalPeriod = rotationalPeriod;
					else
						AdvancedRocketry.logger.warning("rotational Period must be greater than 0"); //TODO: more detailed error msg
				} catch (NumberFormatException e) {
					AdvancedRocketry.logger.warning("Invalid rotational period specified"); //TODO: more detailed error msg
				}
			}
			else if(planetPropertyNode.getNodeName().equalsIgnoreCase("biomeids")) {

				String biomeList[] = planetPropertyNode.getTextContent().split(",");
				for(int j = 0; j < biomeList.length; j++) {
					try {
						int biome =  Integer.parseInt(biomeList[j]);

						if(!properties.addBiome(biome))
							AdvancedRocketry.logger.warning(biomeList[j] + " is not a valid biome id"); //TODO: more detailed error msg
					} catch (NumberFormatException e) {
						AdvancedRocketry.logger.warning(biomeList[j] + " is not a valid biome id"); //TODO: more detailed error msg
					}
				}
			}
			else if(planetPropertyNode.getNodeName().equalsIgnoreCase("planet")) {
				List<DimensionProperties> childList = readPlanetFromNode(planetPropertyNode, star);
				if(childList.size() > 0) {
					DimensionProperties child = childList.get(childList.size()-1); // Last entry in the list is the child planet
					properties.addChildPlanet(child);
					list.addAll(childList);
				}
			}
			else if(planetPropertyNode.getNodeName().equalsIgnoreCase("orbitalPhi")) {
				try {
					properties.orbitalPhi = (Integer.parseInt(planetPropertyNode.getTextContent()) % 360) * 2/Math.PI;
				} catch (NumberFormatException e) {
					AdvancedRocketry.logger.warning("Invalid orbitalTheta specified"); //TODO: more detailed error msg
				}
			}
			else if(planetPropertyNode.getNodeName().equalsIgnoreCase("oreGen")) {
				properties.oreProperties = XMLOreLoader.loadOre(planetPropertyNode);
			}

			planetPropertyNode = planetPropertyNode.getNextSibling();
		}
		
		//Star may not be registered at this time, use ID version instead
		properties.setStar(star.getId());

		//If no biomes are specified add some!
		if(properties.getBiomes().isEmpty())
			properties.addBiomes(properties.getViableBiomes());

		return list;
	}

	
	public StellarBody readStar(Node planetNode) {
		StellarBody star = new StellarBody();
		if(planetNode.hasAttributes()) {
			Node nameNode = planetNode.getAttributes().getNamedItem("name");
			if(nameNode != null && !nameNode.getNodeValue().isEmpty()) {
				star.setName(nameNode.getNodeValue());
			}

			nameNode = planetNode.getAttributes().getNamedItem("temp");

			if(nameNode != null && !nameNode.getNodeValue().isEmpty()) {
				try {
					star.setTemperature(Integer.parseInt(nameNode.getNodeValue()));
				} catch (NumberFormatException e) {
					AdvancedRocketry.logger.warning("Error Reading star " + star.getName());
				}
			}

			nameNode = planetNode.getAttributes().getNamedItem("x");

			if(nameNode != null && !nameNode.getNodeValue().isEmpty()) {
				try {
					star.setPosX(Integer.parseInt(nameNode.getNodeValue()));
				} catch (NumberFormatException e) {
					AdvancedRocketry.logger.warning("Error Reading star " + star.getName());
				}
			}

			nameNode = planetNode.getAttributes().getNamedItem("y");

			if(nameNode != null && !nameNode.getNodeValue().isEmpty()) {
				try {
					star.setPosZ(Integer.parseInt(nameNode.getNodeValue()));
				} catch (NumberFormatException e) {
					AdvancedRocketry.logger.warning("Error Reading star " + star.getName());
				}
			}

			nameNode = planetNode.getAttributes().getNamedItem("numPlanets");
			
			try {
				maxPlanetNumber.put(star ,Integer.parseInt(nameNode.getNodeValue()));
			} catch (Exception e) {
				AdvancedRocketry.logger.warning("Invalid number of planets specified in xml config!");
			}
			
			nameNode = planetNode.getAttributes().getNamedItem("numGasGiants");
			try {
				maxGasPlanetNumber.put(star ,Integer.parseInt(nameNode.getNodeValue()));
			} catch (Exception e) {
				AdvancedRocketry.logger.warning("Invalid number of planets specified in xml config!");
			}
		}

		star.setId(starId++);
		return star;
	}
	
	public DimensionPropertyCoupling readAllPlanets() {
		DimensionPropertyCoupling coupling = new DimensionPropertyCoupling();

		Node masterNode = doc.getElementsByTagName("galaxy").item(0).getFirstChild();

		//readPlanetFromNode changes value
		//Yes it's hacky but that's another reason why it's private

		offset = DimensionManager.dimOffset;
		while(masterNode != null) {
			if(!masterNode.getNodeName().equals("star")) {
				masterNode = masterNode.getNextSibling();
				continue;
			}

			StellarBody star = readStar(masterNode);
			coupling.stars.add(star);

			NodeList planetNodeList = masterNode.getChildNodes();

			Node planetNode = planetNodeList.item(0);

			while(planetNode != null) {
				if(planetNode.getNodeName().equalsIgnoreCase("planet")) {
					coupling.dims.addAll(readPlanetFromNode(planetNode, star));
				}
				planetNode = planetNode.getNextSibling();
			}

			masterNode = masterNode.getNextSibling();
		}
		return coupling;
	}
	
	public static class DimensionPropertyCoupling {
		
		public List<StellarBody> stars = new LinkedList<StellarBody>();
		public List<DimensionProperties> dims = new LinkedList<DimensionProperties>();
		
		
	}
}
