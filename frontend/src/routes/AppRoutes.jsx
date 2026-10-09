import { Route, Routes } from 'react-router-dom'
import ProtectedRoute from '../components/ProtectedRoute'
import LandingPage from '../pages/LandingPage'
import LoginPage from '../pages/LoginPage'
import RegisterPage from '../pages/RegisterPage'
import NotFoundPage from '../pages/NotFoundPage'
import JobsPage from '../pages/jobs/JobsPage'
import JobDetailPage from '../pages/jobs/JobDetailPage'
import CandidateDashboard from '../pages/candidate/CandidateDashboard'
import CandidateApplications from '../pages/candidate/CandidateApplications'
import AIJobMatcher from '../pages/candidate/AIJobMatcher'
import HRDashboard from '../pages/hr/HRDashboard'
import HrJobs from '../pages/hr/HrJobs'
import HrJobForm from '../pages/hr/HrJobForm'
import HrJobApplicants from '../pages/hr/HrJobApplicants'
import { ROLES } from './roles'

function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<LandingPage />} />
      <Route path="/login" element={<LoginPage variant="candidate" />} />
      <Route path="/hr/login" element={<LoginPage variant="hr" />} />
      <Route path="/register" element={<RegisterPage />} />

      <Route path="/jobs" element={<JobsPage />} />
      <Route path="/jobs/:jobId" element={<JobDetailPage />} />

      <Route element={<ProtectedRoute allowedRole={ROLES.CANDIDATE} />}>
        <Route path="/candidate/dashboard" element={<CandidateDashboard />} />
        <Route path="/candidate/applications" element={<CandidateApplications />} />
        <Route path="/candidate/ai-matcher" element={<AIJobMatcher />} />
      </Route>

      <Route element={<ProtectedRoute allowedRole={ROLES.HR} />}>
        <Route path="/hr/dashboard" element={<HRDashboard />} />
        <Route path="/hr/jobs" element={<HrJobs />} />
        <Route path="/hr/jobs/new" element={<HrJobForm />} />
        <Route path="/hr/jobs/:jobId/edit" element={<HrJobForm />} />
        <Route path="/hr/jobs/:jobId/applicants" element={<HrJobApplicants />} />
      </Route>

      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  )
}

export default AppRoutes
