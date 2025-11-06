import { Component, type ErrorInfo, type ReactNode } from 'react';
import { Button } from './ui/Button/Button';
import { AlertTriangle, RefreshCw } from 'lucide-react';

interface Props {
    children: ReactNode;
    fallback?: ReactNode;
}

interface State {
    hasError: boolean;
    error?: Error;
}

class ErrorBoundary extends Component<Props, State> {
    public state: State = {
        hasError: false,
    };

    public static getDerivedStateFromError(error: Error): State {
        return { hasError: true, error };
    }

    public componentDidCatch(error: Error, errorInfo: ErrorInfo) {
        console.error('ErrorBoundary caught an error:', error, errorInfo);
    }

    private handleRetry = () => {
        this.setState({ hasError: false, error: undefined });
    };

    public render() {
        if (this.state.hasError) {
            if (this.props.fallback) {
                return this.props.fallback;
            }

            return (
                <div className="min-h-screen bg-gray-50 flex items-center justify-center p-4">
                    <div className="bg-white p-8 max-w-md w-full text-center">
                        <div className="w-16 h-16 bg-red-100 mx-auto mb-4 flex items-center justify-center">
                            <AlertTriangle className="w-8 h-8 text-red-600" />
                        </div>
                        <h2 className="text-xl font-semibold text-gray-900 mb-2">Đã xảy ra lỗi</h2>
                        <p className="text-gray-600 mb-6">Xin lỗi, đã có lỗi xảy ra khi tải trang. Vui lòng thử lại.</p>
                        <div className="space-y-3">
                            <Button
                                onClick={this.handleRetry}
                                className="w-full bg-blue-600 hover:bg-blue-700 flex items-center justify-center gap-2"
                            >
                                <RefreshCw className="w-4 h-4" />
                                Thử lại
                            </Button>
                            <Button onClick={() => (window.location.href = '/')} variant="outline" className="w-full">
                                Về trang chủ
                            </Button>
                        </div>
                        {process.env.NODE_ENV === 'development' && this.state.error && (
                            <details className="mt-4 text-left">
                                <summary className="cursor-pointer text-sm text-gray-500">
                                    Chi tiết lỗi (Development)
                                </summary>
                                <pre className="mt-2 text-xs text-red-600 bg-red-50 p-2 overflow-auto">
                                    {this.state.error.message}
                                    {this.state.error.stack}
                                </pre>
                            </details>
                        )}
                    </div>
                </div>
            );
        }

        return this.props.children;
    }
}

export default ErrorBoundary;
