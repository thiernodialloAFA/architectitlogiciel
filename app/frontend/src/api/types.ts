export type LifecycleStatus = 'PLANNED' | 'PILOT' | 'ACTIVE' | 'DEPRECATED' | 'RETIRED'
export type IntegrationType = 'REST_API' | 'EVENTS' | 'BATCH_FILE' | 'DATABASE_LINK' | 'SDK'
export type RiskCategory =
  | 'TECHNICAL_DEBT'
  | 'END_OF_LIFE'
  | 'INTEGRATION_BRITTLENESS'
  | 'SECURITY_COMPLIANCE'
  | 'AI_READINESS'
export type RatingLevel = 'LOW' | 'MEDIUM' | 'HIGH'
export type TreatmentDecision = 'TOLERATE' | 'INVEST' | 'MIGRATE' | 'ELIMINATE'
export type RiskStatus = 'OPEN' | 'IN_PROGRESS' | 'MITIGATED' | 'ACCEPTED' | 'CLOSED'
export type AdrStatus = 'PROPOSED' | 'ACCEPTED' | 'DEPRECATED' | 'SUPERSEDED'
export type AdrSource = 'PLATFORM' | 'REPOSITORY'
export type AuditEntityType =
  | 'APPLICATION'
  | 'RISK_ENTRY'
  | 'ADR'
  | 'FORUM_SESSION'
  | 'AAF_PROPOSAL'
  | 'ARBITRATION'
  | 'STANDARD'
  | 'C4_DIAGRAM'
  | 'CHECK_REFERENCE'
export type AuditAction = 'CREATED' | 'FIELD_CHANGED' | 'STATUS_CHANGED'
export type AafScope = 'TEAM' | 'DOMAIN' | 'GROUP'
export type ProposalStatus = 'SUBMITTED' | 'SCHEDULED' | 'DECIDED'
export type ForumSessionStatus = 'PLANNED' | 'HELD'
export type StandardCategory = 'INTEGRATION' | 'API_CONVENTION' | 'AI_PATTERN' | 'SECURITY' | 'DATA'
export type StandardStatus = 'DRAFT' | 'ACTIVE' | 'RETIRED'
export type CheckType = 'FITNESS_FUNCTION' | 'CONTRACT_TEST'
export type CheckStatus = 'PASSING' | 'FAILING' | 'UNKNOWN'

export interface Dependency {
  id: string
  applicationId: string
  applicationName: string
  dependsOnId: string
  dependsOnName: string
  integrationType: IntegrationType
  description: string | null
}

export interface Application {
  id: string
  name: string
  description: string | null
  ownerTeam: string
  ownerDepartment: string
  lifecycleStatus: LifecycleStatus
  c4ModelLink: string | null
  adrLogLink: string | null
  dependencies: Dependency[]
  openRiskCount: number
  createdAt: string
  updatedAt: string
}

export interface ApplicationRequest {
  name: string
  description: string | null
  ownerTeam: string
  ownerDepartment: string
  lifecycleStatus: LifecycleStatus
  c4ModelLink: string | null
  adrLogLink: string | null
}

export interface Risk {
  id: string
  applicationId: string
  applicationName: string
  title: string
  category: RiskCategory
  description: string
  currentControls: string | null
  inherentImpact: RatingLevel
  inherentLikelihood: RatingLevel
  residualImpact: RatingLevel
  residualLikelihood: RatingLevel
  residualScore: number
  blastRadius: string | null
  treatmentDecision: TreatmentDecision
  costToFix: string | null
  targetDate: string | null
  status: RiskStatus
  riskOwner: string
  evidenceLink: string | null
  reviewCadenceDays: number
  lastAssessedAt: string
  stale: boolean
  createdAt: string
  updatedAt: string
}

export interface RiskRequest {
  applicationId: string
  title: string
  category: RiskCategory
  description: string
  currentControls: string | null
  inherentImpact: RatingLevel
  inherentLikelihood: RatingLevel
  residualImpact: RatingLevel
  residualLikelihood: RatingLevel
  blastRadius: string | null
  treatmentDecision: TreatmentDecision
  costToFix: string | null
  targetDate: string | null
  status: RiskStatus
  riskOwner: string
  evidenceLink: string | null
  reviewCadenceDays: number
  lastAssessedAt: string
}

export interface AdrRef {
  id: string
  adrNumber: number
  title: string
}

export interface LinkedRiskRef {
  id: string
  title: string
}

export interface Adr {
  id: string
  adrNumber: number
  title: string
  status: AdrStatus
  context: string
  decision: string
  consequences: string | null
  alternativesConsidered: string | null
  author: string
  department: string | null
  tags: string | null
  aiRelated: boolean
  source: AdrSource
  sourceRepoUrl: string | null
  sourcePath: string | null
  supersedes: AdrRef | null
  supersededBy: AdrRef | null
  linkedRisks: LinkedRiskRef[]
  createdAt: string
  updatedAt: string
}

export interface AdrCreateRequest {
  title: string
  context: string
  decision: string
  consequences: string | null
  alternativesConsidered: string | null
  author: string
  department: string | null
  tags: string | null
  aiRelated: boolean
  linkedRiskIds: string[]
}

export interface AdrUpdateRequest {
  title: string
  context: string
  decision: string
  consequences: string | null
  alternativesConsidered: string | null
  department: string | null
  tags: string | null
  aiRelated: boolean
  linkedRiskIds: string[]
}

export interface AuditEvent {
  id: number
  entityType: AuditEntityType
  entityId: string
  action: AuditAction
  fieldName: string | null
  oldValue: string | null
  newValue: string | null
  actor: string
  occurredAt: string
}

export interface DashboardSummary {
  applicationCount: number
  openRiskCount: number
  staleRiskCount: number
  adrCount: number
  proposedAdrCount: number
  aiAdrCount: number
  openProposalCount: number
  arbitrationCount: number
  activeStandardCount: number
  failingCheckCount: number
  risksByCategory: Record<string, number>
  topRisks: Risk[]
}

export interface AdrRefLite {
  id: string
  adrNumber: number
  title: string
}

export interface ApplicationRefLite {
  id: string
  name: string
}

export interface SessionRefLite {
  id: string
  sessionDate: string
  title: string
}

export interface ForumSession {
  id: string
  sessionDate: string
  title: string
  status: ForumSessionStatus
  notes: string | null
  agendaSize: number
  createdAt: string
  updatedAt: string
}

export interface SessionCreateRequest {
  sessionDate: string
  title: string
  notes: string | null
}

export interface Proposal {
  id: string
  title: string
  summary: string
  scope: AafScope
  status: ProposalStatus
  submittedBy: string
  department: string | null
  adr: AdrRefLite | null
  application: ApplicationRefLite | null
  session: SessionRefLite | null
  adviceGiven: string | null
  advisedBy: string | null
  finalDecision: string | null
  decidedAt: string | null
  arbitrated: boolean
  createdAt: string
  updatedAt: string
}

export interface ProposalCreateRequest {
  title: string
  summary: string
  scope: AafScope
  submittedBy: string
  department: string | null
  adrId: string | null
  applicationId: string | null
}

export interface ProposalOutcomeRequest {
  adviceGiven: string
  advisedBy: string
  finalDecision: string
  resultingAdrId: string | null
}

export interface ArbitrationRequest {
  requestedBy: string
  rationale: string
  outcome: string
  decidedBy: string
}

export interface Arbitration {
  id: string
  proposalId: string
  proposalTitle: string
  requestedBy: string
  rationale: string
  outcome: string
  decidedBy: string
  occurredAt: string
}

export interface SessionDetail {
  session: ForumSession
  agenda: Proposal[]
}

export interface Standard {
  id: string
  title: string
  category: StandardCategory
  status: StandardStatus
  version: number
  content: string
  owner: string
  linkedAdrs: AdrRefLite[]
  appliedApplications: ApplicationRefLite[]
  createdAt: string
  updatedAt: string
}

export interface StandardRequest {
  title: string
  category: StandardCategory
  content: string
  owner: string
  linkedAdrIds: string[]
  appliedApplicationIds: string[]
}

export interface C4Diagram {
  id: string
  applicationId: string
  version: number
  label: string
  source: string
  author: string
  createdAt: string
}

export interface C4DiagramCreateRequest {
  label: string
  source: string
}

export interface CheckReference {
  id: string
  applicationId: string
  applicationName: string
  name: string
  checkType: CheckType
  tool: string
  link: string | null
  description: string | null
  lastStatus: CheckStatus
  lastRunAt: string | null
  createdAt: string
  updatedAt: string
}

export interface CheckCreateRequest {
  name: string
  checkType: CheckType
  tool: string
  link: string | null
  description: string | null
}

export interface AdrImportRequest {
  repoUrl: string
  documents: { path: string; markdown: string }[]
}

export interface AdrImportResultEntry {
  path: string
  adrId: string
  adrNumber: number
  title: string
  outcome: string
}

export interface AdrImportResponse {
  repoUrl: string
  created: number
  updated: number
  results: AdrImportResultEntry[]
}
