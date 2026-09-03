import { Link, useNavigate } from 'react-router-dom'
import { useApplications } from '../api/hooks'
import { Badge, LoadState, PageHeader } from '../components/ui'

export default function LandscapePage() {
  const { data, isLoading, error } = useApplications()
  const navigate = useNavigate()

  return (
    <>
      <PageHeader
        title="Application Landscape"
        subtitle="Inventory of applications with ownership, lifecycle status, and cross-domain integration dependencies — the foundation of the first-year stocktake."
      />
      <LoadState isLoading={isLoading} error={error}>
        {data && (
          <table className="data-table">
            <thead>
              <tr>
                <th>Application</th>
                <th>Owner team</th>
                <th>Department</th>
                <th>Lifecycle</th>
                <th>Dependencies</th>
                <th>Open risks</th>
              </tr>
            </thead>
            <tbody>
              {data.map((app) => (
                <tr key={app.id} className="clickable" onClick={() => navigate(`/landscape/${app.id}`)}>
                  <td>
                    <Link to={`/landscape/${app.id}`} onClick={(e) => e.stopPropagation()}>
                      {app.name}
                    </Link>
                  </td>
                  <td>{app.ownerTeam}</td>
                  <td>{app.ownerDepartment}</td>
                  <td>
                    <Badge value={app.lifecycleStatus} />
                  </td>
                  <td>{app.dependencies.length}</td>
                  <td>{app.openRiskCount}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </LoadState>
    </>
  )
}
