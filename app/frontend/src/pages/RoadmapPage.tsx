import { PageHeader } from '../components/ui'

const ROADMAP = [
  {
    title: 'Architecture Advice Forum & governance workflow',
    detail:
      'Proposal submission with declared scope (team / domain / group), forum session agenda and outcome capture, and an explicit — rarely used — arbitration record for group-scope escalations.',
  },
  {
    title: 'Standards & reference patterns library',
    detail:
      'Curated, versioned integration standards, API conventions and AI-integration patterns, each linked to the ADRs that established them and the landscape entries where they are applied.',
  },
  {
    title: 'C4 model viewer',
    detail:
      'Textual C4 per landscape entry rendered client-side with a pinned Mermaid.js build; diagram source is treated as untrusted input and sandboxed accordingly.',
  },
  {
    title: 'ADR repository indexing (source-of-truth option a)',
    detail:
      'Read-only import of Markdown ADRs from teams that already colocate them in their own repositories, for cross-department search without creating a second source of truth.',
  },
  {
    title: 'Fitness-function & contract-testing status integration',
    detail:
      'Surface ArchUnit / dependency-cruiser CI results and Pact Broker verification status on the relevant landscape entries — the platform tracks that checks exist and pass; it does not reimplement them.',
  },
]

export default function RoadmapPage() {
  return (
    <>
      <PageHeader
        title="Roadmap"
        subtitle="Per the validated proposal (§6): the landscape + risk register + ADR slice is built deeply first; these modules are explicitly labelled as roadmap items rather than shipped as shallow stubs."
      />
      <div className="roadmap-list">
        {ROADMAP.map((item) => (
          <div className="card roadmap-item" key={item.title}>
            <h3>{item.title}</h3>
            <p>{item.detail}</p>
          </div>
        ))}
      </div>
    </>
  )
}
