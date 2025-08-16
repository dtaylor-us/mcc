import React, {useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {mcpService} from '../services/apiService';
import {useApp} from '../App';

type FormData = {
    name: string;
    model: string;
    serialNumber: string;
    location: string;
    manualPath: string;
};

const AdminScreen: React.FC = () => {
    const navigate = useNavigate();
    const {setCurrentAsset} = useApp();
    const [formData, setFormData] = useState<FormData>({
        name: '',
        model: '',
        serialNumber: '',
        location: '',
        manualPath: '',
    });
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const {name, value} = e.target;
        setFormData(prev => ({...prev, [name]: value}));
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!formData.name) {
            setError("Asset Name is required.");
            return;
        }
        setIsSubmitting(true);
        setError(null);
        try {
            const newAsset = await mcpService.createAsset(formData);
            setCurrentAsset(newAsset);
            // The blueprint specifies a success toast, but for simplicity, we navigate directly.
            navigate(`/asset/id/${newAsset.id}`);
        } catch (err) {
            setError(err instanceof Error ? err.message : "Failed to create asset.");
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="max-w-2xl mx-auto bg-slate-800 p-6 md:p-8 rounded-lg shadow-lg">
            <h2 className="text-2xl font-bold mb-6 text-white">Create New Asset</h2>
            {error && <div className="bg-red-900 border border-red-700 text-red-200 px-4 py-3 rounded-md mb-4">{error}</div>}
            <form onSubmit={handleSubmit} className="space-y-4">
                <div>
                    <label htmlFor="name" className="block text-sm font-medium text-slate-300 mb-1">Name</label>
                    <input type="text" name="name" id="name" required value={formData.name} onChange={handleChange}
                           className="w-full px-3 py-2 bg-slate-700 border border-slate-600 rounded-md focus:outline-none focus:ring-2 focus:ring-cyan-500"/>
                </div>
                <div>
                    <label htmlFor="model" className="block text-sm font-medium text-slate-300 mb-1">Model</label>
                    <input type="text" name="model" id="model" value={formData.model} onChange={handleChange}
                           className="w-full px-3 py-2 bg-slate-700 border border-slate-600 rounded-md focus:outline-none focus:ring-2 focus:ring-cyan-500"/>
                </div>
                <div>
                    <label htmlFor="serialNumber" className="block text-sm font-medium text-slate-300 mb-1">Serial Number</label>
                    <input type="text" name="serialNumber" id="serialNumber" value={formData.serialNumber} onChange={handleChange}
                           className="w-full px-3 py-2 bg-slate-700 border border-slate-600 rounded-md focus:outline-none focus:ring-2 focus:ring-cyan-500"/>
                </div>
                <div>
                    <label htmlFor="location" className="block text-sm font-medium text-slate-300 mb-1">Location</label>
                    <input type="text" name="location" id="location" value={formData.location} onChange={handleChange}
                           className="w-full px-3 py-2 bg-slate-700 border border-slate-600 rounded-md focus:outline-none focus:ring-2 focus:ring-cyan-500"/>
                </div>
                <div>
                    <label htmlFor="manualPath" className="block text-sm font-medium text-slate-300 mb-1">Manual File</label>
                    <input
                        type="file"
                        id="manualPath"
                        name="manualPath"
                        onChange={e => {
                            const file = e.target.files?.[0];
                            setFormData(prev => ({
                                ...prev,
                                manualPath: file ? file.name : ''
                            }));
                        }}
                        className="w-full px-3 py-2 bg-slate-700 border border-slate-600 rounded-md focus:outline-none focus:ring-2 focus:ring-cyan-500"
                    />
                </div>
                <div className="pt-4">
                    <button
                        type="submit"
                        disabled={isSubmitting}
                        className="w-full bg-cyan-600 hover:bg-cyan-500 text-white font-bold py-3 px-4 rounded-lg transition-colors disabled:bg-slate-500 disabled:cursor-not-allowed"
                    >
                        {isSubmitting ? 'Creating Asset...' : 'Create Asset and Generate QR'}
                    </button>
                </div>
            </form>
        </div>
    );
};

export default AdminScreen;
