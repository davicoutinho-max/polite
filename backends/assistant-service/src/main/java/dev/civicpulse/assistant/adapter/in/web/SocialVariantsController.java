package dev.civicpulse.assistant.adapter.in.web;

import dev.civicpulse.assistant.adapter.in.web.dto.GenerateSocialVariantsRequest;
import dev.civicpulse.assistant.adapter.in.web.dto.GenerateSocialVariantsResponse;
import dev.civicpulse.assistant.application.port.in.GenerateSocialVariantsUseCase;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Same trust model as BillAssistantController — no account needed to call the model itself,
 * abuse is guarded by request-size limits (see GenerateSocialVariantsRequest) rather than auth.
 * The composer only shows this to authenticated politicians/parties, but the endpoint itself
 * doesn't need to know who's asking. */
@RestController
@RequestMapping("/assistant/posts")
public class SocialVariantsController {

  private final GenerateSocialVariantsUseCase generateSocialVariantsUseCase;

  public SocialVariantsController(GenerateSocialVariantsUseCase generateSocialVariantsUseCase) {
    this.generateSocialVariantsUseCase = generateSocialVariantsUseCase;
  }

  @PostMapping("/social-variants")
  public GenerateSocialVariantsResponse generate(@Valid @RequestBody GenerateSocialVariantsRequest request) {
    return GenerateSocialVariantsResponse.from(generateSocialVariantsUseCase.generate(request.postText()));
  }
}
