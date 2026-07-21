package dev.civicpulse.feedcontent.adapter.in.web;

import dev.civicpulse.feedcontent.adapter.in.web.dto.MediaUploadResponse;
import dev.civicpulse.feedcontent.application.port.out.MediaStorage;
import java.io.IOException;
import java.io.UncheckedIOException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** Upload endpoint backing post attachments (image or generic file) — the returned URL is then
 * passed as {@code imageUrl}/{@code fileUrl} on the actual post-creation call. */
@RestController
@RequestMapping("/media")
public class MediaController {

  private final MediaStorage mediaStorage;

  public MediaController(MediaStorage mediaStorage) {
    this.mediaStorage = mediaStorage;
  }

  @PostMapping
  public MediaUploadResponse upload(@RequestParam("file") MultipartFile file) {
    try {
      String url = mediaStorage.upload(file.getOriginalFilename(), file.getContentType(), file.getInputStream(), file.getSize());
      return new MediaUploadResponse(url, file.getOriginalFilename());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
