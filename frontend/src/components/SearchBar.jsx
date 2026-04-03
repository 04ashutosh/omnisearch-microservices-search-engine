import React, { useState } from "react";
import { Search, Loader2 } from "lucide-react";

export default function SearchBar({ onSearch, loading }) {
    const [query, setQuery] = useState('');

    const handleSubmit = (e) => {
        e.preventDefault();
        if (query.trim()) onSearch(query);
    };

    return (
        <form onSubmit={handleSubmit} className="relative w-full max-w-3xl group">
            <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none 
            text-slate-400 group-focus-within:text-brand-accent transition-colors">
                {loading ? <Loader2 className="animate-spin h-5 w-5" /> : <Search className="h-5 w-5" />}
            </div>
            <input
                type="text"
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                placeholder="What are you searching for today?"
                className="w-full bg-slate-900/60 border border-slate-700 text-slate-100 sm:text-lg 
                rounded-2xl focus:ring-2 focus:ring-brand-accent focus:border-transparent block pl-12 pr-4 py-4 
                backdrop-blur-sm transition-all duration-300 outline-none"
            />
        </form>
    );
}