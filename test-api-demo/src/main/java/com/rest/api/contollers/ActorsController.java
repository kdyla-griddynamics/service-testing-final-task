package com.rest.api.contollers;

import com.rest.api.models.Actor;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.Set;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
public class ActorsController {

    Set<Actor> actors = new HashSet<>();

    ActorsController() {
        actors.add(new Actor(1, "Tom", "Hanks"));
        actors.add(new Actor(2, "Johnny", "Depp"));
        actors.add(new Actor(3, "Robert", "Downey, Jr."));
        actors.add(new Actor(4, "Will", "Smith"));
    }

    @RequestMapping(method = GET, value = "/actors", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation(value = "Get all actors")
    public Set<Actor> getActors() {
        return actors;
    }

    @RequestMapping(method = POST, value = "/actor", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation(value = "Add actor")
    public Actor addActor(@RequestBody Actor actor) {
        actors.add(actor);
        return actor;
    }

    @RequestMapping(method = DELETE, value = "/actor", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation(value = "Delete actor's profile from database")
    public Set<Actor> deleteActor(@RequestBody Actor actor) {
        actors.remove(actor);
        return actors;
    }
}