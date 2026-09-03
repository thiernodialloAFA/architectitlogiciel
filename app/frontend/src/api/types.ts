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
export type AuditEntityType = 'APPLICATION' | 'RISK_ENTRY' | 'ADR'
export type AuditAction = 'CREATED' | 'FIELD_CHANGED' | 'STATUS_CHANGED'

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
  risksByCategory: Record<string, number>
  topRisks: Risk[]
}
