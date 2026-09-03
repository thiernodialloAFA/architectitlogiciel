import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useImportAdrs } from '../api/hooks'
import type { AdrImportResponse } from '../api/types'
import { Badge, PageHeader } from '../components/ui'

export default function AdrImportPage() {
  const importAdrs = useImportAdrs()
  const [repoUrl, setRepoUrl] = useState('')
  const [path, setPath] = useState('')
  const [markdown, setMarkdown] = useState('')
  const [lastResult, setLastResult] = useState<AdrImportResponse | null>(null)

  const onSubmit = (event: React.FormEvent) => {
    event.preventDefault()
    importAdrs.mutate(
      { repoUrl, documents: [{ path, markdown }] },
      {
        onSuccess: (result) => {
          setLastResult(result)
          setPath('')
          setMarkdown('')
        },
      },
    )
  }

  return (
    <>
      <PageHeader
        title="ADR Repository Indexing"
        subtitle="Source-of-truth option (a): teams keep Markdown ADRs in their own repositories; the platform indexes a read-only copy for cross-department search — never a second editable source of truth."
      />

      <div className="card">
        <h2>How it works</h2>
        <p className="prose">
          A repository's CI pipeline POSTs its ADR Markdown files to <code>/api/adr-imports</code> whenever they
          change. Each document is parsed (title, status, context, decision, consequences, alternatives, tags) and
          upserted by its <code>(repository URL, file path)</code> identity — re-importing the same file updates the
          indexed copy instead of duplicating it. Imported ADRs appear in{' '}
          <Link to="/adrs">ADRs &amp; Case Law</Link> with a <Badge value="REPOSITORY" /> badge, are fully searchable,
          and are read-only in the platform: edits belong in the owning repository.
        </p>
      </div>

      <div className="card">
        <h2>Manual import (same endpoint the CI webhook uses)</h2>
        <form onSubmit={onSubmit}>
          <div className="form-grid">
            <div className="field">
              <label htmlFor="repo-url">Repository URL</label>
              <input
                id="repo-url"
                required
                maxLength={500}
                placeholder="https://github.com/org/repository"
                value={repoUrl}
                onChange={(e) => setRepoUrl(e.target.value)}
              />
            </div>
            <div className="field">
              <label htmlFor="path">File path within the repository</label>
              <input
                id="path"
                required
                maxLength={500}
                placeholder="docs/adr/0001-example.md"
                value={path}
                onChange={(e) => setPath(e.target.value)}
              />
            </div>
            <div className="field full">
              <label htmlFor="markdown">ADR Markdown (Nygard / MADR style)</label>
              <textarea
                id="markdown"
                required
                rows={12}
                placeholder={'# Use event-driven integration\n\n## Status\nAccepted\n\n## Context\n…\n\n## Decision\n…'}
                value={markdown}
                onChange={(e) => setMarkdown(e.target.value)}
              />
            </div>
          </div>
          <div className="form-actions">
            <button className="button" type="submit" disabled={importAdrs.isPending}>
              Import / re-index
            </button>
            {importAdrs.error && <span className="error-box">{importAdrs.error.message}</span>}
          </div>
        </form>
      </div>

      {lastResult && (
        <div className="card">
          <h2>Import result</h2>
          <p className="muted">
            {lastResult.created} created, {lastResult.updated} updated from {lastResult.repoUrl}
          </p>
          <table className="data-table">
            <thead>
              <tr>
                <th>Path</th>
                <th>ADR</th>
                <th>Outcome</th>
              </tr>
            </thead>
            <tbody>
              {lastResult.results.map((entry) => (
                <tr key={entry.path}>
                  <td className="nowrap">{entry.path}</td>
                  <td>
                    <Link to={`/adrs/${entry.adrId}`}>
                      ADR-{entry.adrNumber} — {entry.title}
                    </Link>
                  </td>
                  <td>
                    <Badge value={entry.outcome.toUpperCase()} />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </>
  )
}
