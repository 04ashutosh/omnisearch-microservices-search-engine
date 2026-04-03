import { ExternalLink } from "lucide-react";

export default function ResultCard({ item }) {
    return (
        <div className="glass-card hover-glow p-6 transition-all border-l-4 border-l-brand-accent">
            <div className="flex justify-between items-start mb-2">
                <h3 className="text-xl font-bold text-slate-50 group cursor-pointer hover:text-brand-accent transition-colors">
                    {item.title || "Untitled Page"}
                </h3>
                <a href={item.url} target="_blank" rel="noreferrer" className="text-slate-400 hover:text-brand-accent">
                    <ExternalLink className="h-5 w-5" />
                </a>
            </div>
            <p className="text-slate-400 text-sm mb-4 break-all truncate">{item.url}</p>

            {/* Search Snippet with highlighting */}
            <div
                className="text-slate-300 leading-relaxed text-sm"
                dangerouslySetInnerHTML={{ __html: item.snippet }}
            />
        </div>
    );
}
