package com.bimaas.mock.domain;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "project")
public class Project {

	private String projectId;
	private String projectName;
	private String description;
	private String category;
	private Object geoFence;
	private String latitude;
	private String longitude;
	private String type;

	public Project() {
	}

	public Project(String prjId, String prjName, String desc,
			String projectCategory, Object geoFence, String latitude,
			String longitude, String type) {
		this.projectId = prjId;
		this.projectName = prjName;
		this.description = desc;
		this.category = projectCategory;
		this.geoFence = geoFence;
		this.latitude = latitude;
		this.longitude = longitude;
		this.type = type;
	}

	/**
	 * @return the projectId
	 */
	public final String getProjectId() {
		return projectId;
	}

	/**
	 * @param projectId
	 *            the projectId to set
	 */
	public final void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	/**
	 * @return the projectName
	 */
	public final String getProjectName() {
		return projectName;
	}

	/**
	 * @param projectName
	 *            the projectName to set
	 */
	public final void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	/**
	 * @return the description
	 */
	public final String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public final void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the category
	 */
	public final String getCategory() {
		return category;
	}

	/**
	 * @param category
	 *            the category to set
	 */
	public final void setCategory(String category) {
		this.category = category;
	}

	/**
	 * @return the geoFence
	 */
	public final Object getGeoFence() {
		return geoFence;
	}

	/**
	 * @param geoFence
	 *            the geoFence to set
	 */
	public final void setGeoFence(Object geoFence) {
		this.geoFence = geoFence;
	}

	/**
	 * @return the latitude
	 */
	public final String getLatitude() {
		return latitude;
	}

	/**
	 * @param latitude
	 *            the latitude to set
	 */
	public final void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	/**
	 * @return the longitude
	 */
	public final String getLongitude() {
		return longitude;
	}

	/**
	 * @param longitude
	 *            the longitude to set
	 */
	public final void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	/**
	 * @return the type
	 */
	public final String getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public final void setType(String type) {
		this.type = type;
	}

}
