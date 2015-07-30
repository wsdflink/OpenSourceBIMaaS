package com.bimaas.mock.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bimaas.mock.ProjectService;
import com.bimaas.mock.domain.Project;

public class ProjectServiceImpl implements ProjectService {

	private static final Log LOG = LogFactory.getLog(ProjectServiceImpl.class);

	private final ArrayList<Project> response = new ArrayList<Project>();

	public ProjectServiceImpl() {
		Project pr1 = new Project(
				"919",
				"Denvor - Leap District Townhall Project",
				"This project is about Denvor - Leap District Townhall Project ",
				"Construction", null, "23.123", "3.123", "ifc2x3");
		Project pr2 = new Project("918",
				"St Jones Hospital Construction Project ",
				"This project is about St Jones Hospital Construction Project",
				"Facility Management", null, "24.843", "13.923", "ifc2x3");
		Project pr3 = new Project("917", "Perfect Railway Project - NSW ", "This project is about Perfect Railway Project - NSW ",
				"Facility Management", null, "12.983", "53.823", "ifc2x3");
		response.add(pr1);
		response.add(pr2);
		response.add(pr3);
	}

	@Override
	public boolean create(Project project) {
		LOG.info("[MOCK] Creating project");
		return true;
	}

	@Override
	public Project getProject(String projectId) {
		LOG.info("[MOCK] Requesting project: " + projectId);

		Project pr1 = new Project("919", "Citrus", "Hotel Project",
				"Construction", null, "23.123", "43.123", "ifc2x3");
		for (Project prj : response) {
			if (prj.getProjectId().equals(projectId)) {
				return prj;
			}
		}
		return new Project();
	}

	@Override
	public Project update(Project project) {
		LOG.info("[MOCK] Updating project: " + project.getProjectId());
		return project;
	}

	@Override
	public List<Project> getProjects() {
		LOG.info("[MOCK] Requesting all projects");
		return response;
	}

	@Override
	public boolean checkin(String projectId) {
		LOG.info("[MOCK] checkinf for project: " + projectId);
		return true;
	}
}