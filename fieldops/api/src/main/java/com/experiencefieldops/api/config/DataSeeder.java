package com.experiencefieldops.api.config;

import com.experiencefieldops.api.entities.*;
import com.experiencefieldops.api.enums.*;
import com.experiencefieldops.api.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DataSeeder — popula o banco com dados iniciais baseados na collection do Postman.
 *
 * Executa apenas quando o perfil "default" ou "dev" está ativo (não em produção/test).
 * Verifica se os dados já existem antes de inserir (idempotente).
 *
 * Ordem de inserção respeitando dependências:
 *   1. Usuários (ADMIN, SUPERVISOR, TECNICO)
 *   2. Clientes
 *   3. Locais (InspectionSite) → depende de Client
 *   4. Equipamentos             → depende de InspectionSite
 *   5. Templates                → independente
 *   6. TemplateVersions         → depende de InspectionTemplate
 *   7. TemplateSections         → depende de InspectionTemplateVersion
 *   8. TemplateItems            → depende de TemplateSection
 *   9. Inspeções                → depende de Client, Site, User, TemplateVersion, Equipment
 *  10. InspectionItemSnapshots  → depende de Inspection + TemplateItems
 *  11. InspectionResponses      → depende de Inspection + InspectionItemSnapshot
 *  12. NonConformities          → depende de Inspection + InspectionItemSnapshot
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    // ── Repositories ─────────────────────────────────────────────────────────
    private final UserRepository                    userRepository;
    private final ClientRepository                  clientRepository;
    private final InspectionSiteRepository          siteRepository;
    private final EquipmentRepository               equipmentRepository;
    private final InspectionTemplateRepository      templateRepository;
    private final InspectionTemplateVersionRepository templateVersionRepository;
    private final TemplateSectionRepository         sectionRepository;
    private final TemplateItemRepository            itemRepository;
    private final InspectionRepository              inspectionRepository;
    private final InspectionItemSnapshotRepository  snapshotRepository;
    private final InspectionResponseRepository      responseRepository;
    private final NonConformityRepository           nonConformityRepository;

    private final PasswordEncoder passwordEncoder;

    // ── Entry point ──────────────────────────────────────────────────────────
    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.existsByEmail("admin@fieldops.com")) {
            log.info("[DataSeeder] Dados já existem — seed ignorado.");
            return;
        }

        log.info("[DataSeeder] Iniciando seed do banco de dados...");

        // 1. Usuários
        User admin      = seedAdmin();
        User supervisor = seedSupervisor();
        User tecnico    = seedTecnico();
        User tecnico2   = seedTecnico2();

        // 2. Clientes
        Client clienteABC    = seedClienteABC();
        Client clienteXYZ    = seedClienteXYZ();

        // 3. Locais
        InspectionSite siteSpABC     = seedSiteSaoPauloABC(clienteABC);
        InspectionSite siteCpABC     = seedSiteCampinaABC(clienteABC);
        InspectionSite siteRjXYZ     = seedSiteRioXYZ(clienteXYZ);

        // 4. Equipamentos
        Equipment compressorA1  = seedEquipamentoCompressorA1(siteSpABC);
        Equipment geradorB2     = seedEquipamentoGeradorB2(siteSpABC);
        Equipment bombaC3       = seedEquipamentoBombaC3(siteCpABC);
        Equipment cabeiraD4     = seedEquipamentoCabeiraD4(siteRjXYZ);

        // 5. Templates
        InspectionTemplate tmplCompressor  = seedTemplateCompressores();
        InspectionTemplate tmplEletrico    = seedTemplateEletrico();
        InspectionTemplate tmplSeguranca   = seedTemplateSeguranca();

        // 6. Versões dos templates
        InspectionTemplateVersion vCompressor = seedTemplateVersion(tmplCompressor, 1, true);
        InspectionTemplateVersion vEletrico   = seedTemplateVersion(tmplEletrico,   1, true);
        InspectionTemplateVersion vSeguranca  = seedTemplateVersion(tmplSeguranca,  1, true);

        // Atualiza currentVersion no template
        tmplCompressor.setCurrentVersion(1);
        tmplEletrico.setCurrentVersion(1);
        tmplSeguranca.setCurrentVersion(1);
        templateRepository.save(tmplCompressor);
        templateRepository.save(tmplEletrico);
        templateRepository.save(tmplSeguranca);

        // 7 + 8. Seções e Itens dos templates
        seedTemplateSectionsAndItems(vCompressor, vEletrico, vSeguranca);

        // 9. Inspeções (vários cenários de status)
        Inspection insp1 = seedInspectionScheduled(clienteABC, siteSpABC, tecnico, vCompressor, compressorA1);
        Inspection insp2 = seedInspectionInProgress(clienteABC, siteSpABC, tecnico, vCompressor, geradorB2);
        Inspection insp3 = seedInspectionCompleted(clienteABC, siteCpABC, tecnico2, vEletrico, bombaC3);
        Inspection insp4 = seedInspectionCancelled(clienteXYZ, siteRjXYZ, tecnico, vSeguranca, cabeiraD4, admin);
        Inspection insp5 = seedInspectionUnderReview(clienteXYZ, siteRjXYZ, tecnico2, vSeguranca, null);

        // 10 + 11 + 12. Snapshots, Respostas e NCs para inspeções com progresso
        seedSnapshotsAndResponses(insp2, vCompressor);
        seedSnapshotsAndResponses(insp3, vEletrico);
        seedSnapshotsAndResponses(insp5, vSeguranca);
        seedNonConformity(insp2);
        seedNonConformity(insp3);

        log.info("[DataSeeder] Seed concluído com sucesso.");
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 1. USUÁRIOS
    // ═══════════════════════════════════════════════════════════════════════

    private User seedAdmin() {
        // Postman: Register (ADMIN) → name: "Admin FieldOps", email: "admin@fieldops.com", password: "Admin@123"
        return userRepository.save(User.builder()
                .name("Admin FieldOps")
                .email("admin@fieldops.com")
                .passwordHash(passwordEncoder.encode("Admin@123"))
                .role(UserRole.ADMIN)
                .active(true)
                .build());
    }

    private User seedSupervisor() {
        // Postman: Register (SUPERVISOR) → name: "Maria Supervisora", email: "supervisor@fieldops.com"
        return userRepository.save(User.builder()
                .name("Maria Supervisora")
                .email("supervisor@fieldops.com")
                .passwordHash(passwordEncoder.encode("Sup@123"))
                .role(UserRole.SUPERVISOR)
                .active(true)
                .build());
    }

    private User seedTecnico() {
        // Postman: Register (TECNICO) → name: "Carlos Tecnico", email: "tecnico@fieldops.com"
        return userRepository.save(User.builder()
                .name("Carlos Tecnico")
                .email("tecnico@fieldops.com")
                .passwordHash(passwordEncoder.encode("Tec@123"))
                .role(UserRole.TECNICO)
                .active(true)
                .build());
    }

    private User seedTecnico2() {
        // Postman: Criar usuário (via /api/user) → name: "Novo Tecnico", email: "novo.tecnico@fieldops.com"
        return userRepository.save(User.builder()
                .name("Novo Tecnico")
                .email("novo.tecnico@fieldops.com")
                .passwordHash(passwordEncoder.encode("Senha@123"))
                .role(UserRole.TECNICO)
                .active(true)
                .build());
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 2. CLIENTES
    // ═══════════════════════════════════════════════════════════════════════

    private Client seedClienteABC() {
        // Postman: Criar cliente → name: "Empresa ABC", document: "12.345.678/0001-90"
        return clientRepository.save(Client.builder()
                .name("Empresa ABC")
                .document("12.345.678/0001-90")
                .email("contato@abc.com")
                .phone("(11) 99999-0000")
                .status(ClientStatus.ACTIVE)
                .build());
    }

    private Client seedClienteXYZ() {
        return clientRepository.save(Client.builder()
                .name("Empresa XYZ")
                .document("98.765.432/0001-10")
                .email("contato@xyz.com")
                .phone("(21) 98888-1234")
                .status(ClientStatus.ACTIVE)
                .build());
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 3. LOCAIS (InspectionSite)
    // ═══════════════════════════════════════════════════════════════════════

    private InspectionSite seedSiteSaoPauloABC(Client client) {
        // Postman: Criar local → name: "Filial São Paulo", address: "Av. Paulista, 1000, São Paulo - SP"
        return siteRepository.save(InspectionSite.builder()
                .name("Filial São Paulo")
                .address("Av. Paulista, 1000, São Paulo - SP")
                .latitude(-23.5613)
                .longitude(-46.6563)
                .status(SiteStatus.ACTIVE)
                .client(client)
                .build());
    }

    private InspectionSite seedSiteCampinaABC(Client client) {
        return siteRepository.save(InspectionSite.builder()
                .name("Filial Campinas")
                .address("Rua Barão de Jaguara, 500, Campinas - SP")
                .latitude(-22.9056)
                .longitude(-47.0608)
                .status(SiteStatus.ACTIVE)
                .client(client)
                .build());
    }

    private InspectionSite seedSiteRioXYZ(Client client) {
        return siteRepository.save(InspectionSite.builder()
                .name("Matriz Rio de Janeiro")
                .address("Av. Rio Branco, 200, Rio de Janeiro - RJ")
                .latitude(-22.9068)
                .longitude(-43.1729)
                .status(SiteStatus.ACTIVE)
                .client(client)
                .build());
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 4. EQUIPAMENTOS
    // ═══════════════════════════════════════════════════════════════════════

    private Equipment seedEquipamentoCompressorA1(InspectionSite site) {
        // Postman: Criar equipamento → name: "Compressor Industrial A1"
        return equipmentRepository.save(Equipment.builder()
                .name("Compressor Industrial A1")
                .description("Compressor de ar 100HP Atlas Copco")
                .qrCode(UUID.randomUUID().toString())
                .status(EquipmentStatus.ACTIVE)
                .site(site)
                .build());
    }

    private Equipment seedEquipamentoGeradorB2(InspectionSite site) {
        return equipmentRepository.save(Equipment.builder()
                .name("Gerador Elétrico B2")
                .description("Gerador 250kVA Cummins - uso emergencial")
                .qrCode(UUID.randomUUID().toString())
                .status(EquipmentStatus.ACTIVE)
                .site(site)
                .build());
    }

    private Equipment seedEquipamentoBombaC3(InspectionSite site) {
        return equipmentRepository.save(Equipment.builder()
                .name("Bomba Hidráulica C3")
                .description("Bomba centrífuga 50HP para fluidos industriais")
                .qrCode(UUID.randomUUID().toString())
                .status(EquipmentStatus.ACTIVE)
                .site(site)
                .build());
    }

    private Equipment seedEquipamentoCabeiraD4(InspectionSite site) {
        return equipmentRepository.save(Equipment.builder()
                .name("Caldeira Industrial D4")
                .description("Caldeira a vapor 5t/h — manutenção preventiva mensal")
                .qrCode(UUID.randomUUID().toString())
                .status(EquipmentStatus.INACTIVE)
                .site(site)
                .build());
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 5. TEMPLATES
    // ═══════════════════════════════════════════════════════════════════════

    private InspectionTemplate seedTemplateCompressores() {
        // Postman: Criar template → title: "Inspeção de Compressores", category: "MECANICO"
        return templateRepository.save(InspectionTemplate.builder()
                .title("Inspeção de Compressores")
                .description("Checklist para inspeção de compressores industriais")
                .category("MECANICO")
                .status(TemplateStatus.ACTIVE)
                .currentVersion(0)
                .build());
    }

    private InspectionTemplate seedTemplateEletrico() {
        return templateRepository.save(InspectionTemplate.builder()
                .title("Inspeção Elétrica Geral")
                .description("Checklist de inspeção de painéis elétricos e geradores")
                .category("ELETRICO")
                .status(TemplateStatus.ACTIVE)
                .currentVersion(0)
                .build());
    }

    private InspectionTemplate seedTemplateSeguranca() {
        return templateRepository.save(InspectionTemplate.builder()
                .title("Inspeção de Segurança do Trabalho")
                .description("Checklist de itens de segurança e EPIs")
                .category("SEGURANCA")
                .status(TemplateStatus.ACTIVE)
                .currentVersion(0)
                .build());
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 6. VERSÕES DOS TEMPLATES
    // ═══════════════════════════════════════════════════════════════════════

    private InspectionTemplateVersion seedTemplateVersion(
            InspectionTemplate template, int versionNumber, boolean active) {
        return templateVersionRepository.save(InspectionTemplateVersion.builder()
                .template(template)
                .versionNumber(versionNumber)
                .titleSnapshot(template.getTitle())
                .descriptionSnapshot(template.getDescription())
                .activeForNewInspections(active)
                .publishedAt(LocalDateTime.now())
                .build());
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 7 + 8. SEÇÕES E ITENS DOS TEMPLATES
    // ═══════════════════════════════════════════════════════════════════════

    private void seedTemplateSectionsAndItems(
            InspectionTemplateVersion vCompressor,
            InspectionTemplateVersion vEletrico,
            InspectionTemplateVersion vSeguranca) {

        // ── Template Compressores ──────────────────────────────────────────
        // Postman: Criar seção → title: "Inspeção Visual"
        TemplateSection secVisual = sectionRepository.save(TemplateSection.builder()
                .templateVersion(vCompressor)
                .title("Inspeção Visual")
                .description("Verificações visuais do equipamento")
                .displayOrder(0)
                .build());

        TemplateSection secMecanica = sectionRepository.save(TemplateSection.builder()
                .templateVersion(vCompressor)
                .title("Verificação Mecânica")
                .description("Verificações de componentes mecânicos e estruturais")
                .displayOrder(1)
                .build());

        TemplateSection secOperacional = sectionRepository.save(TemplateSection.builder()
                .templateVersion(vCompressor)
                .title("Parâmetros Operacionais")
                .description("Leitura e registro de parâmetros de operação")
                .displayOrder(2)
                .build());

        // Itens da seção "Inspeção Visual"
        // Postman: Criar item (BOOLEAN) → code: "VIS-001", title: "Existe vazamento de oleo?"
        itemRepository.save(TemplateItem.builder()
                .section(secVisual)
                .code("VIS-001")
                .title("Existe vazamento de óleo?")
                .description("Verificar presença de vazamento no selo mecânico")
                .responseType(ResponseType.BOOLEAN)
                .required(true)
                .observationRequiredOnFailure(true)
                .evidenceRequiredOnFailure(true)
                .displayOrder(0)
                .build());

        // Postman: Criar item (CONFORMITY) → code: "VIS-002"
        itemRepository.save(TemplateItem.builder()
                .section(secVisual)
                .code("VIS-002")
                .title("Condição geral do equipamento")
                .description("Avaliar conformidade geral do equipamento")
                .responseType(ResponseType.CONFORMITY)
                .required(true)
                .observationRequiredOnFailure(true)
                .evidenceRequiredOnFailure(false)
                .displayOrder(1)
                .build());

        // Postman: Criar item (SINGLE_CHOICE) → code: "VIS-003"
        itemRepository.save(TemplateItem.builder()
                .section(secVisual)
                .code("VIS-003")
                .title("Nível de ruído do equipamento")
                .description("Selecionar nível de ruído percebido")
                .responseType(ResponseType.SINGLE_CHOICE)
                .required(true)
                .observationRequiredOnFailure(false)
                .evidenceRequiredOnFailure(false)
                .optionsJson("[\"Baixo\",\"Medio\",\"Alto\",\"Critico\"]")
                .displayOrder(2)
                .build());

        // Postman: Criar item (TEXT_SHORT) → code: "VIS-004"
        itemRepository.save(TemplateItem.builder()
                .section(secVisual)
                .code("VIS-004")
                .title("Observações gerais")
                .description("Registro de observações adicionais do técnico")
                .responseType(ResponseType.TEXT_SHORT)
                .required(false)
                .observationRequiredOnFailure(false)
                .evidenceRequiredOnFailure(false)
                .displayOrder(3)
                .build());

        // Itens da seção "Verificação Mecânica"
        itemRepository.save(TemplateItem.builder()
                .section(secMecanica)
                .code("MEC-001")
                .title("Correia de transmissão em bom estado?")
                .description("Verificar tensão e desgaste da correia")
                .responseType(ResponseType.BOOLEAN)
                .required(true)
                .observationRequiredOnFailure(true)
                .evidenceRequiredOnFailure(true)
                .displayOrder(0)
                .build());

        itemRepository.save(TemplateItem.builder()
                .section(secMecanica)
                .code("MEC-002")
                .title("Fixação dos parafusos de ancoragem")
                .description("Verificar torque e integridade dos parafusos de base")
                .responseType(ResponseType.CONFORMITY)
                .required(true)
                .observationRequiredOnFailure(true)
                .evidenceRequiredOnFailure(false)
                .displayOrder(1)
                .build());

        itemRepository.save(TemplateItem.builder()
                .section(secMecanica)
                .code("MEC-003")
                .title("Data da última manutenção preventiva")
                .responseType(ResponseType.DATE)
                .required(false)
                .observationRequiredOnFailure(false)
                .evidenceRequiredOnFailure(false)
                .displayOrder(2)
                .build());

        // Itens da seção "Parâmetros Operacionais"
        itemRepository.save(TemplateItem.builder()
                .section(secOperacional)
                .code("OP-001")
                .title("Pressão de operação (bar)")
                .description("Registrar pressão atual no manômetro principal")
                .responseType(ResponseType.NUMBER)
                .required(true)
                .observationRequiredOnFailure(false)
                .evidenceRequiredOnFailure(false)
                .displayOrder(0)
                .build());

        itemRepository.save(TemplateItem.builder()
                .section(secOperacional)
                .code("OP-002")
                .title("Temperatura do óleo (°C)")
                .description("Registrar temperatura do óleo lubrificante")
                .responseType(ResponseType.NUMBER)
                .required(true)
                .observationRequiredOnFailure(false)
                .evidenceRequiredOnFailure(false)
                .displayOrder(1)
                .build());

        itemRepository.save(TemplateItem.builder()
                .section(secOperacional)
                .code("OP-003")
                .title("Observações do operador")
                .responseType(ResponseType.TEXT_LONG)
                .required(false)
                .observationRequiredOnFailure(false)
                .evidenceRequiredOnFailure(false)
                .displayOrder(2)
                .build());

        // ── Template Elétrico ──────────────────────────────────────────────
        TemplateSection secPainel = sectionRepository.save(TemplateSection.builder()
                .templateVersion(vEletrico)
                .title("Painel Elétrico")
                .description("Verificação do estado do painel e disjuntores")
                .displayOrder(0)
                .build());

        TemplateSection secCabeamento = sectionRepository.save(TemplateSection.builder()
                .templateVersion(vEletrico)
                .title("Cabeamento e Aterramento")
                .description("Verificação de cabos, conexões e sistema de aterramento")
                .displayOrder(1)
                .build());

        itemRepository.save(TemplateItem.builder()
                .section(secPainel)
                .code("ELE-001")
                .title("Disjuntores em bom estado?")
                .description("Verificar ausência de queima ou tripping espúrio")
                .responseType(ResponseType.BOOLEAN)
                .required(true)
                .observationRequiredOnFailure(true)
                .evidenceRequiredOnFailure(true)
                .displayOrder(0)
                .build());

        itemRepository.save(TemplateItem.builder()
                .section(secPainel)
                .code("ELE-002")
                .title("Tensão de alimentação (V)")
                .description("Medir tensão nas fases do painel principal")
                .responseType(ResponseType.NUMBER)
                .required(true)
                .observationRequiredOnFailure(false)
                .evidenceRequiredOnFailure(false)
                .displayOrder(1)
                .build());

        itemRepository.save(TemplateItem.builder()
                .section(secPainel)
                .code("ELE-003")
                .title("Condição da identificação dos circuitos")
                .description("Verificar se os circuitos estão corretamente identificados")
                .responseType(ResponseType.CONFORMITY)
                .required(true)
                .observationRequiredOnFailure(true)
                .evidenceRequiredOnFailure(false)
                .displayOrder(2)
                .build());

        itemRepository.save(TemplateItem.builder()
                .section(secCabeamento)
                .code("CAB-001")
                .title("Cabos com isolamento íntegro?")
                .responseType(ResponseType.BOOLEAN)
                .required(true)
                .observationRequiredOnFailure(true)
                .evidenceRequiredOnFailure(true)
                .displayOrder(0)
                .build());

        itemRepository.save(TemplateItem.builder()
                .section(secCabeamento)
                .code("CAB-002")
                .title("Sistema de aterramento")
                .description("Avaliar conformidade do sistema de aterramento")
                .responseType(ResponseType.CONFORMITY)
                .required(true)
                .observationRequiredOnFailure(true)
                .evidenceRequiredOnFailure(false)
                .displayOrder(1)
                .build());

        // ── Template Segurança ─────────────────────────────────────────────
        TemplateSection secEpi = sectionRepository.save(TemplateSection.builder()
                .templateVersion(vSeguranca)
                .title("EPIs e Equipamentos de Segurança")
                .description("Verificação do uso e estado dos EPIs")
                .displayOrder(0)
                .build());

        TemplateSection secEmergencia = sectionRepository.save(TemplateSection.builder()
                .templateVersion(vSeguranca)
                .title("Sistemas de Emergência")
                .description("Verificação de extintores, saídas de emergência e PPCI")
                .displayOrder(1)
                .build());

        itemRepository.save(TemplateItem.builder()
                .section(secEpi)
                .code("SEG-001")
                .title("Técnico utilizando EPI completo?")
                .description("Verificar capacete, óculos, luvas e botina de segurança")
                .responseType(ResponseType.BOOLEAN)
                .required(true)
                .observationRequiredOnFailure(true)
                .evidenceRequiredOnFailure(false)
                .displayOrder(0)
                .build());

        itemRepository.save(TemplateItem.builder()
                .section(secEpi)
                .code("SEG-002")
                .title("Estado geral dos EPIs")
                .description("Verificar desgaste e vencimento dos equipamentos")
                .responseType(ResponseType.CONFORMITY)
                .required(true)
                .observationRequiredOnFailure(true)
                .evidenceRequiredOnFailure(false)
                .displayOrder(1)
                .build());

        itemRepository.save(TemplateItem.builder()
                .section(secEpi)
                .code("SEG-003")
                .title("Tipo de EPI de proteção respiratória em uso")
                .responseType(ResponseType.SINGLE_CHOICE)
                .required(false)
                .observationRequiredOnFailure(false)
                .evidenceRequiredOnFailure(false)
                .optionsJson("[\"Nenhum\",\"Mascara PFF1\",\"Mascara PFF2\",\"Mascara PFF3\",\"SCBA\"]")
                .displayOrder(2)
                .build());

        itemRepository.save(TemplateItem.builder()
                .section(secEmergencia)
                .code("EMG-001")
                .title("Extintor de incêndio dentro da validade?")
                .description("Verificar data de validade e lacre do extintor")
                .responseType(ResponseType.BOOLEAN)
                .required(true)
                .observationRequiredOnFailure(true)
                .evidenceRequiredOnFailure(true)
                .displayOrder(0)
                .build());

        itemRepository.save(TemplateItem.builder()
                .section(secEmergencia)
                .code("EMG-002")
                .title("Saídas de emergência desobstruídas?")
                .responseType(ResponseType.BOOLEAN)
                .required(true)
                .observationRequiredOnFailure(true)
                .evidenceRequiredOnFailure(false)
                .displayOrder(1)
                .build());

        itemRepository.save(TemplateItem.builder()
                .section(secEmergencia)
                .code("EMG-003")
                .title("Conformidade geral da área com NR-12")
                .description("Avaliar conformidade da área conforme NR-12")
                .responseType(ResponseType.CONFORMITY)
                .required(true)
                .observationRequiredOnFailure(true)
                .evidenceRequiredOnFailure(true)
                .displayOrder(2)
                .build());

        itemRepository.save(TemplateItem.builder()
                .section(secEmergencia)
                .code("EMG-004")
                .title("Observações de segurança adicionais")
                .responseType(ResponseType.TEXT_LONG)
                .required(false)
                .observationRequiredOnFailure(false)
                .evidenceRequiredOnFailure(false)
                .displayOrder(3)
                .build());
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 9. INSPEÇÕES (múltiplos status)
    // ═══════════════════════════════════════════════════════════════════════

    /** SCHEDULED — agendada, ainda não iniciada */
    private Inspection seedInspectionScheduled(
            Client client, InspectionSite site, User tecnico,
            InspectionTemplateVersion version, Equipment equipment) {
        // Postman: Criar inspeção → scheduledDate: "2026-10-15"
        return inspectionRepository.save(Inspection.builder()
                .client(client)
                .site(site)
                .assignedTo(tecnico)
                .templateVersion(version)
                .equipment(equipment)
                .scheduledDate(LocalDate.of(2026, 10, 15))
                .status(InspectionStatus.SCHEDULED)
                .build());
    }

    /** IN_PROGRESS — iniciada pelo técnico */
    private Inspection seedInspectionInProgress(
            Client client, InspectionSite site, User tecnico,
            InspectionTemplateVersion version, Equipment equipment) {
        // Postman: Iniciar inspeção → startedAtDevice: "2026-10-15T08:00:00"
        return inspectionRepository.save(Inspection.builder()
                .client(client)
                .site(site)
                .assignedTo(tecnico)
                .templateVersion(version)
                .equipment(equipment)
                .scheduledDate(LocalDate.of(2026, 10, 15))
                .status(InspectionStatus.IN_PROGRESS)
                .startedAtDevice(LocalDateTime.of(2026, 10, 15, 8, 0, 0))
                .startedAtServer(LocalDateTime.of(2026, 10, 15, 8, 0, 5))
                .build());
    }

    /** COMPLETED — concluída pelo técnico, aguardando revisão */
    private Inspection seedInspectionCompleted(
            Client client, InspectionSite site, User tecnico,
            InspectionTemplateVersion version, Equipment equipment) {
        // Postman: Concluir inspeção → completedAtDevice: "2026-10-15T10:00:00"
        return inspectionRepository.save(Inspection.builder()
                .client(client)
                .site(site)
                .assignedTo(tecnico)
                .templateVersion(version)
                .equipment(equipment)
                .scheduledDate(LocalDate.of(2026, 10, 14))
                .status(InspectionStatus.COMPLETED)
                .startedAtDevice(LocalDateTime.of(2026, 10, 14, 7, 30, 0))
                .startedAtServer(LocalDateTime.of(2026, 10, 14, 7, 30, 8))
                .completedAtDevice(LocalDateTime.of(2026, 10, 14, 10, 0, 0))
                .completedAtServer(LocalDateTime.of(2026, 10, 14, 10, 0, 12))
                .build());
    }

    /** CANCELLED — cancelada com justificativa */
    private Inspection seedInspectionCancelled(
            Client client, InspectionSite site, User tecnico,
            InspectionTemplateVersion version, Equipment equipment, User cancelledBy) {
        // Postman: Cancelar inspeção → reason: "Equipamento indisponivel para inspecao no dia agendado"
        return inspectionRepository.save(Inspection.builder()
                .client(client)
                .site(site)
                .assignedTo(tecnico)
                .templateVersion(version)
                .equipment(equipment)
                .scheduledDate(LocalDate.of(2026, 10, 10))
                .status(InspectionStatus.CANCELLED)
                .cancelledBy(cancelledBy)
                .cancelledAt(LocalDateTime.of(2026, 10, 9, 16, 30, 0))
                .cancellationReason("Equipamento indisponível para inspeção no dia agendado")
                .build());
    }

    /** UNDER_REVIEW — inspeção concluída e enviada para revisão do supervisor */
    private Inspection seedInspectionUnderReview(
            Client client, InspectionSite site, User tecnico,
            InspectionTemplateVersion version, Equipment equipment) {
        return inspectionRepository.save(Inspection.builder()
                .client(client)
                .site(site)
                .assignedTo(tecnico)
                .templateVersion(version)
                .equipment(equipment)
                .scheduledDate(LocalDate.of(2026, 10, 12))
                .status(InspectionStatus.UNDER_REVIEW)
                .startedAtDevice(LocalDateTime.of(2026, 10, 12, 9, 0, 0))
                .startedAtServer(LocalDateTime.of(2026, 10, 12, 9, 0, 6))
                .completedAtDevice(LocalDateTime.of(2026, 10, 12, 11, 30, 0))
                .completedAtServer(LocalDateTime.of(2026, 10, 12, 11, 30, 9))
                .build());
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 10 + 11. SNAPSHOTS E RESPOSTAS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Cria snapshots dos itens do template para a inspeção e registra
     * respostas simulando o trabalho do técnico em campo.
     */
    private void seedSnapshotsAndResponses(
            Inspection inspection, InspectionTemplateVersion version) {

        var sections = sectionRepository.findByTemplateVersionIdOrderByDisplayOrder(version.getId());

        for (var section : sections) {
            var items = itemRepository.findBySectionIdOrderByDisplayOrder(section.getId());

            for (var item : items) {
                // Cria snapshot imutável do item
                InspectionItemSnapshot snapshot = snapshotRepository.save(
                        InspectionItemSnapshot.builder()
                                .inspection(inspection)
                                .originalItem(item)
                                .code(item.getCode())
                                .title(item.getTitle())
                                .description(item.getDescription())
                                .responseType(item.getResponseType())
                                .required(item.getRequired())
                                .observationRequiredOnFailure(item.getObservationRequiredOnFailure())
                                .evidenceRequiredOnFailure(item.getEvidenceRequiredOnFailure())
                                .optionsJson(item.getOptionsJson())
                                .sectionTitle(section.getTitle())
                                .sectionDisplayOrder(section.getDisplayOrder())
                                .itemDisplayOrder(item.getDisplayOrder())
                                .build());

                // Registra resposta simulada baseada no tipo de resposta
                // Postman: POST /inspections/{id}/responses → snapshotItemId, value, observation, notApplicable
                String value = buildSampleValue(item.getResponseType(), item.getCode());

                responseRepository.save(InspectionResponse.builder()
                        .inspection(inspection)
                        .snapshotItem(snapshot)
                        .value(value)
                        .observation(needsObservation(item) ? "Verificado em campo — sem anomalias adicionais." : null)
                        .notApplicable(false)
                        .build());
            }
        }
    }

    /** Gera um valor de exemplo coerente com o responseType do item */
    private String buildSampleValue(ResponseType type, String code) {
        return switch (type) {
            case BOOLEAN          -> "true";
            case CONFORMITY       -> ConformityValue.CONFORMING.name();
            case NUMBER           -> buildNumberSample(code);
            case TEXT_SHORT       -> "Verificado — dentro dos padrões.";
            case TEXT_LONG        -> "Inspeção realizada conforme procedimento. Não foram identificadas anomalias durante a vistoria.";
            case SINGLE_CHOICE    -> buildSingleChoiceSample(code);
            case DATE             -> LocalDate.now().minusMonths(1).toString();
        };
    }

    private String buildNumberSample(String code) {
        if (code == null) return "0";
        return switch (code) {
            case "OP-001"  -> "8.5";    // pressão em bar
            case "OP-002"  -> "65.0";   // temperatura em °C
            case "ELE-002" -> "220.0";  // tensão em V
            default        -> "1.0";
        };
    }

    private String buildSingleChoiceSample(String code) {
        if ("VIS-003".equals(code)) return "Baixo";
        if ("SEG-003".equals(code)) return "Mascara PFF2";
        return "Opcao 1";
    }

    private boolean needsObservation(TemplateItem item) {
        // Adiciona observação apenas em itens que têm observationRequiredOnFailure = true
        // Aqui o valor é "true" (conforme), então a observação é opcional mas adicionamos como exemplo
        return item.getObservationRequiredOnFailure();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 12. NÃO CONFORMIDADES
    // ═══════════════════════════════════════════════════════════════════════

    /** Cria uma não conformidade de exemplo vinculada à inspeção */
    private void seedNonConformity(Inspection inspection) {
        // Postman: POST /inspections/{id}/non-conformities
        // → title, description, severity, snapshotItemId

        // Busca o primeiro snapshot da inspeção para vincular
        var snapshots = snapshotRepository
                .findByInspectionIdOrderBySectionDisplayOrderAscItemDisplayOrderAsc(inspection.getId());

        if (snapshots.isEmpty()) return;

        var snapshot = snapshots.get(0);

        nonConformityRepository.save(NonConformity.builder()
                .inspection(inspection)
                .snapshotItem(snapshot)
                .title("Vazamento de óleo identificado no selo mecânico")
                .description("Durante a inspeção visual foi identificado vazamento de óleo no selo mecânico " +
                        "do equipamento. O volume estimado é de aproximadamente 50ml/h. " +
                        "Necessário substituição do selo em até 72 horas.")
                .severity(Severity.HIGH)
                .status(NonConformityStatus.OPEN)
                .evidenceAttached(false)
                .build());

        // Segunda NC — severidade MEDIUM com status RESOLVED
        if (snapshots.size() > 1) {
            nonConformityRepository.save(NonConformity.builder()
                    .inspection(inspection)
                    .snapshotItem(snapshots.get(1))
                    .title("Ruído acima do esperado durante operação")
                    .description("Foi registrado nível de ruído elevado (estimado acima de 85dB) " +
                            "durante ciclo de operação do compressor.")
                    .severity(Severity.MEDIUM)
                    .status(NonConformityStatus.RESOLVED)
                    .evidenceAttached(true)
                    .build());
        }
    }
}
