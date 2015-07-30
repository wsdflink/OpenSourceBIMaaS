package com.bimaas.mock;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import com.bimaas.mock.domain.Project;

@Path("/")
public interface ProjectService {

	@POST
	@Consumes("application/json")
	@Path("/create")
	public boolean create(Project project);

	@POST
	@Produces("application/json")
	@Path("/{projectId}")
	public Project getProject(@PathParam("projectId") String projectId);

	@POST
	@Consumes("application/json")
	@Produces("application/json")
	@Path("/update")
	public Project update(Project project);

	@POST
	@Produces("application/json")
	@Path("/all")
	public List<Project> getProjects();

	@POST
	@Consumes("application/json")
	@Produces("application/json")
	@Path("/checkin/{projectId}")
	public boolean checkin(@PathParam("projectId") String projectId);

}