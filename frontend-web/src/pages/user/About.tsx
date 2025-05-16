import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft, BookOpen, PencilRuler, Award, LineChart, Heart } from 'lucide-react';

const About = () => {
  const navigate = useNavigate();
  const [version, setVersion] = useState('1.0.0');

  // Get app version from package.json or environment variable
  useEffect(() => {
    // In a real implementation, this could come from an API or environment variable
    // For now, we'll hardcode it to match the mobile app
    setVersion('1.0.0');
  }, []);

  return (
    <div className="min-h-screen bg-base-100">
      {/* Header */}
      <div className="navbar bg-base-100 shadow-sm">
        <div className="flex-none">
          <button 
            className="btn btn-ghost btn-circle"
            onClick={() => navigate(-1)}
          >
            <ArrowLeft className="h-5 w-5" />
          </button>
        </div>
        <div className="flex-1">
          <h1 className="text-xl font-semibold">About EnglishTek</h1>
        </div>
        <div className="flex-none"></div>
      </div>

      {/* Content */}
      <div className="container mx-auto px-4 py-6 max-w-3xl">
        {/* Logo and Version */}
        <div className="flex flex-col items-center justify-center py-8">
          <img 
            src="/src/assets/Logo.png" 
            alt="EnglishTek Logo" 
            className="w-32 h-32 object-contain mb-4"
          />
          <p className="text-base-content/60 text-sm">Version {version}</p>
        </div>

        {/* Our Mission */}
        <div className="card bg-base-100 shadow-md mb-6">
          <div className="card-body">
            <h2 className="card-title text-xl">Our Mission</h2>
            <p className="text-base-content/80 leading-relaxed">
              EnglishTek is dedicated to making English language learning accessible, 
              engaging, and effective for everyone. We combine modern teaching methods 
              with interactive technology to create a unique learning experience.
            </p>
          </div>
        </div>

        {/* Key Features */}
        <div className="card bg-base-100 shadow-md mb-6">
          <div className="card-body">
            <h2 className="card-title text-xl">Key Features</h2>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mt-4">
              <div className="flex items-center p-4 bg-base-200 rounded-lg">
                <div className="w-10 h-10 flex items-center justify-center bg-primary/10 text-primary rounded-full mr-4">
                  <BookOpen className="w-5 h-5" />
                </div>
                <span>Interactive Lessons</span>
              </div>
              <div className="flex items-center p-4 bg-base-200 rounded-lg">
                <div className="w-10 h-10 flex items-center justify-center bg-primary/10 text-primary rounded-full mr-4">
                  <PencilRuler className="w-5 h-5" />
                </div>
                <span>Engaging Quizzes</span>
              </div>
              <div className="flex items-center p-4 bg-base-200 rounded-lg">
                <div className="w-10 h-10 flex items-center justify-center bg-primary/10 text-primary rounded-full mr-4">
                  <Award className="w-5 h-5" />
                </div>
                <span>Achievement Badges</span>
              </div>
              <div className="flex items-center p-4 bg-base-200 rounded-lg">
                <div className="w-10 h-10 flex items-center justify-center bg-primary/10 text-primary rounded-full mr-4">
                  <LineChart className="w-5 h-5" />
                </div>
                <span>Progress Tracking</span>
              </div>
            </div>
          </div>
        </div>

        {/* Learning Approach */}
        <div className="card bg-base-100 shadow-md mb-6">
          <div className="card-body">
            <h2 className="card-title text-xl">Learning Approach</h2>
            <p className="text-base-content/80 leading-relaxed">
              Our step-by-step learning path ensures you build a strong foundation 
              in English. Each chapter is carefully designed to reinforce previous 
              lessons while introducing new concepts in an intuitive way.
            </p>
          </div>
        </div>

        {/* Made with Love */}
        <div className="card bg-base-100 shadow-md mb-6">
          <div className="card-body">
            <h2 className="card-title text-xl">Made with <Heart className="h-5 w-5 text-red-500 inline" /></h2>
            <p className="text-base-content/80 leading-relaxed">
              EnglishTek is crafted with care by a team passionate about education 
              and technology. We're committed to continuously improving and expanding 
              our platform to better serve our learners.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default About;
