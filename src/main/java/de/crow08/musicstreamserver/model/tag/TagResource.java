package de.crow08.musicstreamserver.model.tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tags")
public class TagResource {

  private final TagRepository tagRepository;

  public TagResource(TagRepository tagRepository) {
    this.tagRepository = tagRepository;
  }

  @GetMapping("/all")
  public @ResponseBody Iterable<Tag> getTag() {
    return tagRepository.findAll();
  }

  @PostMapping(path = "/")
  public @ResponseBody long createNewTag(@RequestBody String name) {
    Tag tag = new Tag(name);
    tagRepository.save(tag);
    return tag.getId();
  }
}
