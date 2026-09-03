import { Link } from 'react-router-dom'
import { Badge, PageHeader } from '../components/ui'

const DELIVERED = [
  {
    title: 'Architecture Advice Forum & governance workflow',
    detail:
      'Proposal submission with declared scope (team / domain / group), forum session agenda and outcome capture, and an explicit — rarely used — arbitration record for group-scope escalations.',
    link: '/forum',
    linkLabel: 'Open the Advice Forum',
  },
  {
    title: 'Standards & reference patterns library',
    detail:
      'Curated, versioned integration standards, API conventions and AI-integration patterns, each linked to the ADRs that established them and the landscape entries where they are applied.',
    link: '/standards',
    linkLabel: 'Browse the standards library',
  },
  {
    title: 'C4 model viewer',
    detail:
      'Textual C4 per landscape entry rendered client-side with a pinned Mermaid.js build; diagram source is treated as untrusted input and sandboxed accordingly. Versions are append-only.',
    link: '/landscape',
    linkLabel: 'View diagrams on any landscape entry',
  },
  {
    title: 'ADR repository indexing (source-of-truth option a)',
    detail:
      'Read-only import of Markdown ADRs from teams that already colocate them in their own repositories, for cross-department search without creating a second source of truth.',
    link: '/adr-import',
    linkLabel: 'Import repository ADRs',
  },
  {
    title: 'Fitness-function & contract-testing status integration',
    detail:
      'ArchUnit / dependency-cruiser and Pact check references live on each landscape entry with their last CI-reported status — the platform tracks that checks exist and pass; it does not reimplement them.',
    link: '/landscape',
    linkLabel: 'See checks on any landscape entry',
  },
]

const REMAINING = [
  {
    title: 'Enterprise authentication & roles',
    detail:
      'Replace the demo X-Actor header with OIDC/SSO login, per-department roles, and enforcement of who may activate standards or record arbitrations.',
  },
  {
    title: 'Scheduled repository pulls',
    detail:
      'Complement the push-style ADR import endpoint with scheduled Git pulls so repository ADRs stay indexed even when a team has no CI hook.',
  },
  {
    title: 'Live CI & Pact Broker integration',
    detail:
      'Authenticated webhooks from the group CI platform and the Pact Broker to update check statuses automatically, replacing the manual status reporting used in the demo.',
  },
]

export default function RoadmapPage() {
  return (
    <>
      <PageHeader
        title="Roadmap"
        subtitle="Per the validated proposal (§6): the landscape + risk register + ADR slice was built deeply first (v1); the five roadmap modules were delivered in v2. What remains is production hardening."
      />
      <h2>Delivered in v2</h2>
      <div className="roadmap-list">
        {DELIVERED.map((item) => (
          <div className="card roadmap-item" key={item.title}>
            <h3>
              {item.title} <Badge value="ACTIVE" />
            </h3>
            <p>{item.detail}</p>
            <p>
              <Link to={item.link}>{item.linkLabel} →</Link>
            </p>
          </div>
        ))}
      </div>
      <h2>Remaining (production hardening)</h2>
      <div className="roadmap-list">
        {REMAINING.map((item) => (
          <div className="card roadmap-item" key={item.title}>
            <h3>{item.title}</h3>
            <p>{item.detail}</p>
          </div>
        ))}
      </div>
    </>
  )
}
