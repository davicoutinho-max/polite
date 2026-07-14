package dev.civicpulse.legislative.application;

import dev.civicpulse.legislative.application.port.in.PoliticianDossierUseCase;
import dev.civicpulse.legislative.application.port.out.MandateRepository;
import dev.civicpulse.legislative.application.port.out.PoliticianDossierRepository;
import dev.civicpulse.legislative.application.port.out.SocialLinkRepository;
import dev.civicpulse.legislative.application.port.out.TeamMemberRepository;
import dev.civicpulse.legislative.domain.exception.DossierNotFoundException;
import dev.civicpulse.legislative.domain.model.Mandate;
import dev.civicpulse.legislative.domain.model.PoliticianDossierExtension;
import dev.civicpulse.legislative.domain.model.SocialLink;
import dev.civicpulse.legislative.domain.model.SocialPlatform;
import dev.civicpulse.legislative.domain.model.TeamMember;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PoliticianDossierService implements PoliticianDossierUseCase {

  private final PoliticianDossierRepository dossierRepository;
  private final MandateRepository mandateRepository;
  private final SocialLinkRepository socialLinkRepository;
  private final TeamMemberRepository teamMemberRepository;

  public PoliticianDossierService(
      PoliticianDossierRepository dossierRepository,
      MandateRepository mandateRepository,
      SocialLinkRepository socialLinkRepository,
      TeamMemberRepository teamMemberRepository) {
    this.dossierRepository = dossierRepository;
    this.mandateRepository = mandateRepository;
    this.socialLinkRepository = socialLinkRepository;
    this.teamMemberRepository = teamMemberRepository;
  }

  @Override
  public PoliticianDossierExtension getDossier(UUID politicianAccountId) {
    return dossierRepository.findById(politicianAccountId).orElseThrow(() -> new DossierNotFoundException(politicianAccountId));
  }

  @Override
  @Transactional
  public PoliticianDossierExtension updateDossier(
      UUID politicianAccountId, String education, String profession, String patrimony, String email, String phone, String officeDetail) {
    PoliticianDossierExtension dossier = getDossier(politicianAccountId);
    dossier.updateDossier(education, profession, patrimony, email, phone, officeDetail);
    return dossierRepository.save(dossier);
  }

  @Override
  @Transactional
  public Mandate addMandate(UUID politicianAccountId, String role, String period, boolean current) {
    requireDossierExists(politicianAccountId);
    return mandateRepository.save(Mandate.add(politicianAccountId, role, period, current));
  }

  @Override
  public List<Mandate> getMandates(UUID politicianAccountId) {
    return mandateRepository.findByPolitician(politicianAccountId);
  }

  @Override
  @Transactional
  public SocialLink addSocialLink(UUID politicianAccountId, SocialPlatform platform, String label, String handle, String url) {
    requireDossierExists(politicianAccountId);
    return socialLinkRepository.save(SocialLink.add(politicianAccountId, platform, label, handle, url));
  }

  @Override
  public List<SocialLink> getSocialLinks(UUID politicianAccountId) {
    return socialLinkRepository.findByPolitician(politicianAccountId);
  }

  @Override
  @Transactional
  public TeamMember addTeamMember(UUID politicianAccountId, String name, String role, String avatarUrl) {
    requireDossierExists(politicianAccountId);
    return teamMemberRepository.save(TeamMember.add(politicianAccountId, name, role, avatarUrl));
  }

  @Override
  public List<TeamMember> getTeamMembers(UUID politicianAccountId) {
    return teamMemberRepository.findByPolitician(politicianAccountId);
  }

  private void requireDossierExists(UUID politicianAccountId) {
    if (!dossierRepository.existsById(politicianAccountId)) {
      throw new DossierNotFoundException(politicianAccountId);
    }
  }
}
