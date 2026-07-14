package dev.civicpulse.legislative.application.port.in;

import dev.civicpulse.legislative.domain.model.Mandate;
import dev.civicpulse.legislative.domain.model.PoliticianDossierExtension;
import dev.civicpulse.legislative.domain.model.SocialLink;
import dev.civicpulse.legislative.domain.model.SocialPlatform;
import dev.civicpulse.legislative.domain.model.TeamMember;
import java.util.List;
import java.util.UUID;

public interface PoliticianDossierUseCase {

  PoliticianDossierExtension getDossier(UUID politicianAccountId);

  PoliticianDossierExtension updateDossier(
      UUID politicianAccountId, String education, String profession, String patrimony, String email, String phone, String officeDetail);

  Mandate addMandate(UUID politicianAccountId, String role, String period, boolean current);

  List<Mandate> getMandates(UUID politicianAccountId);

  SocialLink addSocialLink(UUID politicianAccountId, SocialPlatform platform, String label, String handle, String url);

  List<SocialLink> getSocialLinks(UUID politicianAccountId);

  TeamMember addTeamMember(UUID politicianAccountId, String name, String role, String avatarUrl);

  List<TeamMember> getTeamMembers(UUID politicianAccountId);
}
