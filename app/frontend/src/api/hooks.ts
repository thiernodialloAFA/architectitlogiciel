import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api } from './client'
import type {
  Adr,
  AdrCreateRequest,
  AdrImportRequest,
  AdrImportResponse,
  AdrStatus,
  AdrUpdateRequest,
  Application,
  ApplicationRequest,
  Arbitration,
  ArbitrationRequest,
  AuditEntityType,
  AuditEvent,
  C4Diagram,
  C4DiagramCreateRequest,
  CheckCreateRequest,
  CheckReference,
  CheckStatus,
  DashboardSummary,
  ForumSession,
  Proposal,
  ProposalCreateRequest,
  ProposalOutcomeRequest,
  Risk,
  RiskRequest,
  SessionCreateRequest,
  SessionDetail,
  Standard,
  StandardRequest,
  StandardStatus,
} from './types'

export function useDashboard() {
  return useQuery({
    queryKey: ['dashboard'],
    queryFn: () => api.get<DashboardSummary>('/dashboard/summary'),
  })
}

export function useApplications() {
  return useQuery({
    queryKey: ['applications'],
    queryFn: () => api.get<Application[]>('/applications'),
  })
}

export function useApplication(id: string) {
  return useQuery({
    queryKey: ['applications', id],
    queryFn: () => api.get<Application>(`/applications/${id}`),
    enabled: id.length > 0,
  })
}

export function useSaveApplication(id?: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (request: ApplicationRequest) =>
      id
        ? api.put<Application>(`/applications/${id}`, request)
        : api.post<Application>('/applications', request),
    onSuccess: () => queryClient.invalidateQueries(),
  })
}

export function useRisks(filters?: { category?: string; status?: string; applicationId?: string }) {
  const params = new URLSearchParams()
  if (filters?.category) params.set('category', filters.category)
  if (filters?.status) params.set('status', filters.status)
  if (filters?.applicationId) params.set('applicationId', filters.applicationId)
  const suffix = params.size > 0 ? `?${params.toString()}` : ''
  return useQuery({
    queryKey: ['risks', filters ?? {}],
    queryFn: () => api.get<Risk[]>(`/risks${suffix}`),
  })
}

export function useRisk(id: string) {
  return useQuery({
    queryKey: ['risks', id],
    queryFn: () => api.get<Risk>(`/risks/${id}`),
    enabled: id.length > 0,
  })
}

export function useTopRisks(limit = 10) {
  return useQuery({
    queryKey: ['risks', 'top', limit],
    queryFn: () => api.get<Risk[]>(`/risks/top-priority?limit=${limit}`),
  })
}

export function useStaleRisks() {
  return useQuery({
    queryKey: ['risks', 'stale'],
    queryFn: () => api.get<Risk[]>('/risks/stale'),
  })
}

export function useSaveRisk(id?: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (request: RiskRequest) =>
      id ? api.put<Risk>(`/risks/${id}`, request) : api.post<Risk>('/risks', request),
    onSuccess: () => queryClient.invalidateQueries(),
  })
}

export function useAdrs(aiOnly = false) {
  return useQuery({
    queryKey: ['adrs', { aiOnly }],
    queryFn: () => api.get<Adr[]>(`/adrs?aiOnly=${aiOnly}`),
  })
}

export function useAdr(id: string) {
  return useQuery({
    queryKey: ['adrs', id],
    queryFn: () => api.get<Adr>(`/adrs/${id}`),
    enabled: id.length > 0,
  })
}

export function useAdrSearch(query: string) {
  return useQuery({
    queryKey: ['adrs', 'search', query],
    queryFn: () => api.get<Adr[]>(`/adrs/search?q=${encodeURIComponent(query)}`),
    enabled: query.trim().length > 0,
  })
}

export function useCreateAdr() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (request: AdrCreateRequest) => api.post<Adr>('/adrs', request),
    onSuccess: () => queryClient.invalidateQueries(),
  })
}

export function useUpdateAdr(id: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (request: AdrUpdateRequest) => api.put<Adr>(`/adrs/${id}`, request),
    onSuccess: () => queryClient.invalidateQueries(),
  })
}

export function useChangeAdrStatus(id: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (request: { status: AdrStatus; supersededById?: string }) =>
      api.post<Adr>(`/adrs/${id}/status`, request),
    onSuccess: () => queryClient.invalidateQueries(),
  })
}

export function useAuditTrail(entityType: AuditEntityType, entityId: string) {
  return useQuery({
    queryKey: ['audit', entityType, entityId],
    queryFn: () =>
      api.get<AuditEvent[]>(`/audit-events?entityType=${entityType}&entityId=${entityId}`),
    enabled: entityId.length > 0,
  })
}

// --- v2: Architecture Advice Forum ---

export function useForumSessions() {
  return useQuery({
    queryKey: ['forum', 'sessions'],
    queryFn: () => api.get<ForumSession[]>('/forum/sessions'),
  })
}

export function useForumSession(id: string) {
  return useQuery({
    queryKey: ['forum', 'sessions', id],
    queryFn: () => api.get<SessionDetail>(`/forum/sessions/${id}`),
    enabled: id.length > 0,
  })
}

export function useCreateSession() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (request: SessionCreateRequest) =>
      api.post<ForumSession>('/forum/sessions', request),
    onSuccess: () => queryClient.invalidateQueries(),
  })
}

export function useHoldSession(id: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (request: { notes: string | null }) =>
      api.post<ForumSession>(`/forum/sessions/${id}/hold`, request),
    onSuccess: () => queryClient.invalidateQueries(),
  })
}

export function useProposals(filters?: { status?: string }) {
  const params = new URLSearchParams()
  if (filters?.status) params.set('status', filters.status)
  const suffix = params.size > 0 ? `?${params.toString()}` : ''
  return useQuery({
    queryKey: ['forum', 'proposals', filters ?? {}],
    queryFn: () => api.get<Proposal[]>(`/forum/proposals${suffix}`),
  })
}

export function useProposal(id: string) {
  return useQuery({
    queryKey: ['forum', 'proposals', id],
    queryFn: () => api.get<Proposal>(`/forum/proposals/${id}`),
    enabled: id.length > 0,
  })
}

export function useCreateProposal() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (request: ProposalCreateRequest) =>
      api.post<Proposal>('/forum/proposals', request),
    onSuccess: () => queryClient.invalidateQueries(),
  })
}

export function useScheduleProposal(id: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (request: { sessionId: string }) =>
      api.post<Proposal>(`/forum/proposals/${id}/schedule`, request),
    onSuccess: () => queryClient.invalidateQueries(),
  })
}

export function useRecordOutcome(id: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (request: ProposalOutcomeRequest) =>
      api.post<Proposal>(`/forum/proposals/${id}/outcome`, request),
    onSuccess: () => queryClient.invalidateQueries(),
  })
}

export function useArbitrate(id: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (request: ArbitrationRequest) =>
      api.post<Arbitration>(`/forum/proposals/${id}/arbitration`, request),
    onSuccess: () => queryClient.invalidateQueries(),
  })
}

export function useArbitrations() {
  return useQuery({
    queryKey: ['forum', 'arbitrations'],
    queryFn: () => api.get<Arbitration[]>('/forum/arbitrations'),
  })
}

// --- v2: Standards & reference patterns library ---

export function useStandards(filters?: { category?: string }) {
  const params = new URLSearchParams()
  if (filters?.category) params.set('category', filters.category)
  const suffix = params.size > 0 ? `?${params.toString()}` : ''
  return useQuery({
    queryKey: ['standards', filters ?? {}],
    queryFn: () => api.get<Standard[]>(`/standards${suffix}`),
  })
}

export function useStandard(id: string) {
  return useQuery({
    queryKey: ['standards', id],
    queryFn: () => api.get<Standard>(`/standards/${id}`),
    enabled: id.length > 0,
  })
}

export function useSaveStandard(id?: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (request: StandardRequest) =>
      id ? api.put<Standard>(`/standards/${id}`, request) : api.post<Standard>('/standards', request),
    onSuccess: () => queryClient.invalidateQueries(),
  })
}

export function useChangeStandardStatus(id: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (request: { status: StandardStatus }) =>
      api.post<Standard>(`/standards/${id}/status`, request),
    onSuccess: () => queryClient.invalidateQueries(),
  })
}

// --- v2: C4 model viewer ---

export function useC4Diagrams(applicationId: string) {
  return useQuery({
    queryKey: ['c4', applicationId],
    queryFn: () => api.get<C4Diagram[]>(`/applications/${applicationId}/c4`),
    enabled: applicationId.length > 0,
  })
}

export function useAddC4Version(applicationId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (request: C4DiagramCreateRequest) =>
      api.post<C4Diagram>(`/applications/${applicationId}/c4`, request),
    onSuccess: () => queryClient.invalidateQueries(),
  })
}

// --- v2: fitness-function / contract-testing status ---

export function useChecks(applicationId: string) {
  return useQuery({
    queryKey: ['checks', applicationId],
    queryFn: () => api.get<CheckReference[]>(`/applications/${applicationId}/checks`),
    enabled: applicationId.length > 0,
  })
}

export function useCreateCheck(applicationId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (request: CheckCreateRequest) =>
      api.post<CheckReference>(`/applications/${applicationId}/checks`, request),
    onSuccess: () => queryClient.invalidateQueries(),
  })
}

export function useUpdateCheckStatus(id: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (request: { status: CheckStatus; runAt: string | null }) =>
      api.post<CheckReference>(`/checks/${id}/status`, request),
    onSuccess: () => queryClient.invalidateQueries(),
  })
}

// --- v2: ADR repository indexing ---

export function useImportAdrs() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (request: AdrImportRequest) =>
      api.post<AdrImportResponse>('/adr-imports', request),
    onSuccess: () => queryClient.invalidateQueries(),
  })
}
