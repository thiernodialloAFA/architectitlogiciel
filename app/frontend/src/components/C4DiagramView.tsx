import { useEffect, useState } from 'react'

let renderSeq = 0

interface RenderResult {
  source: string
  svg?: string
  error?: string
}

/**
 * Renders C4-in-Mermaid source client-side with a pinned Mermaid build (proposal §3.4).
 * Diagram source is treated as untrusted input: securityLevel 'strict' keeps Mermaid's
 * built-in sanitisation on (script/click handling disabled, labels HTML-encoded), the
 * library is lazy-loaded so it never touches pages that do not render diagrams, and
 * parse/render failures are contained to an inline error box.
 */
export default function C4DiagramView({ source }: { source: string }) {
  const [result, setResult] = useState<RenderResult | null>(null)

  useEffect(() => {
    let cancelled = false
    async function render() {
      try {
        const mermaid = (await import('mermaid')).default
        mermaid.initialize({
          startOnLoad: false,
          securityLevel: 'strict',
          theme: 'neutral',
          suppressErrorRendering: true,
        })
        const { svg } = await mermaid.render(`c4-diagram-${++renderSeq}`, source)
        if (!cancelled) setResult({ source, svg })
      } catch (err) {
        if (!cancelled) setResult({ source, error: err instanceof Error ? err.message : String(err) })
      }
    }
    void render()
    return () => {
      cancelled = true
    }
  }, [source])

  const current = result && result.source === source ? result : null
  if (!current) return <p className="muted">Rendering diagram…</p>
  if (current.error) {
    return (
      <div>
        <p className="error-box">Diagram failed to render: {current.error}</p>
        <pre className="c4-source">{source}</pre>
      </div>
    )
  }
  // Safe: SVG produced by Mermaid under securityLevel 'strict', which sanitises the
  // untrusted diagram source (DOMPurify) before emitting markup.
  return <div className="c4-canvas" dangerouslySetInnerHTML={{ __html: current.svg ?? '' }} />
}
