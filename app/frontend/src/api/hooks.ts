import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api } from './client'
import type {
  Adr,
  AdrCreateRequest,
  AdrStatus,
  AdrUpdateRequest,
  Application,
  ApplicationRequest,
  AuditEntityType,
  AuditEvent,
  DashboardSummary,
  Risk,
  RiskRequest,
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
