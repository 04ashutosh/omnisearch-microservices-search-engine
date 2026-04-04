import axios from "axios";
import { AlertTriangle, Globe, Search, Zap } from "lucide-react";
import React, { useState } from "react";

const API_GATEWAY = "http://localhost:8765";

// ── inline styles ──────────────────────────────────────────────────────────────
const s = {
  root: {
    minHeight: "100vh",
    background: "#05080f",
    fontFamily: "'DM Sans', sans-serif",
    color: "#e2e8f0",
    position: "relative",
  },
  noiseBg: {
    position: "fixed", top: 0, left: 0, right: 0, bottom: 0, zIndex: 0,
    backgroundImage: `
      radial-gradient(ellipse 80% 60% at 50% -10%, rgba(56,165,255,0.12) 0%, transparent 70%),
      radial-gradient(ellipse 40% 40% at 85% 80%, rgba(99,102,241,0.08) 0%, transparent 60%)`,
    pointerEvents: "none",
  },
  wrap: {
    position: "relative", zIndex: 1,
    maxWidth: 760, margin: "0 auto", padding: "64px 24px 80px",
  },
  header: { marginBottom: 56 },
  logoRow: { display: "flex", alignItems: "center", gap: 14, marginBottom: 10 },
  logoIcon: {
    width: 40, height: 40, borderRadius: 10,
    background: "linear-gradient(135deg,#1e88e5,#5c6bc0)",
    display: "flex", alignItems: "center", justifyContent: "center",
  },
  logoText: {
    fontFamily: "'Space Mono', monospace", fontSize: 26,
    fontWeight: 700, letterSpacing: -1, color: "#fff", margin: 0,
  },
  logoAccent: { color: "#60a5fa" },
  badge: {
    display: "inline-block", background: "rgba(29,78,216,0.15)",
    color: "#60a5fa", borderRadius: 4, padding: "1px 7px",
    fontSize: 11, fontFamily: "'Space Mono', monospace",
    border: "1px solid rgba(96,165,250,0.2)",
  },
  tagline: { fontSize: 14, color: "#64748b", margin: 0 },

  // search bar
  searchBox: (focused) => ({
    display: "flex", alignItems: "center",
    background: "#0d1117",
    border: `1px solid ${focused ? "#2563eb" : "#1e293b"}`,
    borderRadius: 12, overflow: "hidden",
    boxShadow: focused ? "0 0 0 3px rgba(37,99,235,0.15)" : "none",
    transition: "border-color 0.2s, box-shadow 0.2s",
  }),
  searchPrefix: {
    fontFamily: "'Space Mono', monospace", fontSize: 13,
    color: "#334155", padding: "0 14px", whiteSpace: "nowrap", userSelect: "none",
  },
  searchInput: {
    flex: 1, background: "transparent", border: "none", outline: "none",
    fontFamily: "'Space Mono', monospace", fontSize: 15,
    color: "#e2e8f0", padding: "16px 0", caretColor: "#60a5fa",
  },
  searchBtn: {
    margin: 6, background: "#1d4ed8", border: "none", borderRadius: 8,
    padding: "10px 18px", cursor: "pointer",
    display: "flex", alignItems: "center", gap: 8,
    fontFamily: "'DM Sans', sans-serif", fontSize: 14,
    fontWeight: 500, color: "#fff", whiteSpace: "nowrap",
    transition: "background 0.15s",
  },

  // loading bar
  loadingWrap: {
    height: 2, background: "#0d1117", borderRadius: 1, overflow: "hidden", marginTop: 12,
  },
  loadingInner: {
    height: "100%", width: "40%",
    background: "linear-gradient(90deg, transparent, #2563eb, transparent)",
    animation: "sweep 1.2s ease-in-out infinite", borderRadius: 1,
  },

  // meta row
  metaRow: {
    display: "flex", alignItems: "center", gap: 8,
    fontSize: 12, color: "#475569", marginBottom: 20,
    fontFamily: "'Space Mono', monospace",
  },
  metaDot: {
    width: 5, height: 5, borderRadius: "50%", background: "#10b981", flexShrink: 0,
  },

  // error
  errorCard: {
    background: "rgba(127,29,29,0.2)", border: "1px solid rgba(239,68,68,0.25)",
    borderRadius: 10, padding: "18px 20px",
    display: "flex", alignItems: "flex-start", gap: 14,
  },
  errorTitle: { fontSize: 14, fontWeight: 500, color: "#fca5a5", margin: "0 0 3px" },
  errorSub: { fontSize: 13, color: "#f87171", opacity: 0.75, margin: 0 },

  // result cards
  resultsList: { display: "flex", flexDirection: "column", gap: 3 },
  resultCard: {
    background: "#0d1117", border: "1px solid #131b2a",
    borderRadius: 10, padding: "18px 20px", cursor: "pointer",
    transition: "border-color 0.15s, background 0.15s",
  },
  resultDomain: {
    fontFamily: "'Space Mono', monospace", fontSize: 11,
    color: "#334155", margin: "0 0 5px",
    display: "flex", alignItems: "center", gap: 6,
  },
  resultDot: {
    width: 5, height: 5, borderRadius: "50%", background: "#1d4ed8", flexShrink: 0,
  },
  resultTitle: { fontSize: 16, fontWeight: 500, color: "#93c5fd", margin: "0 0 6px", lineHeight: 1.4 },
  resultSnippet: { fontSize: 13, color: "#475569", margin: 0, lineHeight: 1.6 },

  emptyState: { textAlign: "center", padding: "60px 0" },
  emptyText: { fontSize: 14, color: "#334155", fontFamily: "'Space Mono', monospace" },
};

// ── components ──────────────────────────────────────────────────────────────────
function SearchBar({ onSearch, loading }) {
  const [query, setQuery] = useState("");
  const [focused, setFocused] = useState(false);

  const submit = () => { if (query.trim()) onSearch(query.trim()); };

  return (
    <div>
      <style>{`@keyframes sweep{0%{transform:translateX(-200%)}100%{transform:translateX(400%)}}`}</style>
      <div style={s.searchBox(focused)}>
        <span style={s.searchPrefix}>query://</span>
        <input
          style={s.searchInput}
          placeholder="type anything..."
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          onKeyDown={(e) => e.key === "Enter" && submit()}
          onFocus={() => setFocused(true)}
          onBlur={() => setFocused(false)}
          autoComplete="off"
        />
        <button style={s.searchBtn} onClick={submit} disabled={loading}>
          <Search size={16} color="#fff" />
          {loading ? "Searching…" : "Search"}
        </button>
      </div>
      {loading && (
        <div style={s.loadingWrap}>
          <div style={s.loadingInner} />
        </div>
      )}
    </div>
  );
}

function ResultCard({ item }) {
  const [hovered, setHovered] = useState(false);
  const domain = item.url ? new URL(item.url).hostname : "result";
  return (
    <div
      style={{ ...s.resultCard, ...(hovered ? { borderColor: "#1e3a5f", background: "#0f1826" } : {}) }}
      onMouseEnter={() => setHovered(true)}
      onMouseLeave={() => setHovered(false)}
    >
      <p style={s.resultDomain}>
        <span style={s.resultDot} />
        {domain}
      </p>
      <p style={s.resultTitle}>{item.title}</p>
      {/* FIX: Use item.snippet and dangerouslySetInnerHTML */}
      <p 
        style={s.resultSnippet} 
        dangerouslySetInnerHTML={{ __html: item.snippet }} 
      />
    </div>
  );
}

// ── main ────────────────────────────────────────────────────────────────────────
export default function App() {
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [stats, setStats] = useState(null);

  const performSearch = async (query) => {
    setLoading(true);
    setError(null);
    const start = performance.now();
    try {
      const token = localStorage.getItem("jwt_token");
      const response = await axios.get(`${API_GATEWAY}/search?q=${query}`, {
        headers: token ? { Authorization: `Bearer ${token}` } : {},
      });
      setResults(response.data);
      setStats({ count: response.data.length, time: (performance.now() - start).toFixed(0) });
    } catch (err) {
      setError(err.response?.status === 401
        ? "Unauthorized: Please login again."
        : "Search engine is offline (500).");
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <link rel="preconnect" href="https://fonts.googleapis.com" />
      <link href="https://fonts.googleapis.com/css2?family=Space+Mono:wght@400;700&family=DM+Sans:wght@300;400;500&display=swap" rel="stylesheet" />
      <div style={s.root}>
        <div style={s.noiseBg} />
        <div style={s.wrap}>

          {/* Header */}
          <div style={s.header}>
            <div style={s.logoRow}>
              <div style={s.logoIcon}>
                <Globe size={20} color="#fff" />
              </div>
              <h1 style={s.logoText}>
                OMNI<span style={s.logoAccent}>SEARCH</span>
              </h1>
              <span style={s.badge}>v2.0</span>
            </div>
            <p style={s.tagline}>Distributed full-text search — fast by design.</p>
          </div>

          {/* Search */}
          <div style={{ marginBottom: 32 }}>
            <SearchBar onSearch={performSearch} loading={loading} />
          </div>

          {/* Meta */}
          {stats && !error && (
            <div style={s.metaRow}>
              <span style={s.metaDot} />
              <span>{stats.count} results · {stats.time}ms · gateway: localhost:8765</span>
            </div>
          )}

          {/* Error */}
          {error && (
            <div style={{ ...s.errorCard, marginBottom: 20 }}>
              <AlertTriangle size={20} color="#f87171" style={{ flexShrink: 0, marginTop: 1 }} />
              <div>
                <p style={s.errorTitle}>Access Denied / System Offline</p>
                <p style={s.errorSub}>{error}</p>
              </div>
            </div>
          )}

          {/* Results */}
          <div style={s.resultsList}>
            {results.map((item, idx) => <ResultCard key={idx} item={item} />)}
          </div>

          {/* Empty */}
          {!loading && results.length === 0 && !error && stats && (
            <div style={s.emptyState}>
              <p style={{ fontSize: 28, opacity: 0.2, margin: "0 0 14px" }}>◎</p>
              <p style={s.emptyText}>no pages found in index.</p>
            </div>
          )}

        </div>
      </div>
    </>
  );
}