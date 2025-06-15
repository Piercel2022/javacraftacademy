import React, { useState } from 'react';
import styles from './Lesson.module.css'

const Lesson = ({ lessonId = 1 }) => {
  const id = lessonId;
  const [currentStep, setCurrentStep] = useState(0);
  const [codeAnswer, setCodeAnswer] = useState('');

  const lesson = {
    id: id || 1,
    title: "Variables and Data Types in Java",
    course: "Java Fundamentals",
    duration: "45 min",
    steps: [
      {
        type: "content",
        title: "Introduction to Variables",
        content: `
          <h3>What are Variables?</h3>
          <p>Variables in Java are containers that store data values. Think of them as labeled boxes where you can store different types of information.</p>
          
          <h4>Key Points:</h4>
          <ul>
            <li>Variables must be declared before use</li>
            <li>Each variable has a specific data type</li>
            <li>Variable names should be descriptive</li>
          </ul>
          
          <h4>Example:</h4>
          <pre><code>int age = 25;
String name = "John";
double salary = 50000.50;</code></pre>
        `
      },
      {
        type: "content",
        title: "Primitive Data Types",
        content: `
          <h3>Java Primitive Data Types</h3>
          <p>Java has 8 primitive data types that store simple values:</p>
          
          <div class="data-types-grid">
            <div class="data-type">
              <h4>int</h4>
              <p>Whole numbers (-2,147,483,648 to 2,147,483,647)</p>
              <code>int count = 10;</code>
            </div>
            
            <div class="data-type">
              <h4>double</h4>
              <p>Decimal numbers</p>
              <code>double price = 19.99;</code>
            </div>
            
            <div class="data-type">
              <h4>boolean</h4>
              <p>True or false values</p>
              <code>boolean isActive = true;</code>
            </div>
            
            <div class="data-type">
              <h4>char</h4>
              <p>Single characters</p>
              <code>char grade = 'A';</code>
            </div>
          </div>
        `
      },
      {
        type: "quiz",
        title: "Quick Quiz",
        question: "Which data type would you use to store a person's age?",
        options: ["String", "int", "boolean", "char"],
        correct: 1
      },
      {
        type: "coding",
        title: "Practice Exercise",
        instructions: "Create variables to store student information: name (String), age (int), and GPA (double).",
        startingCode: "// Create three variables here:\n// 1. A String variable called 'name'\n// 2. An int variable called 'age'\n// 3. A double variable called 'gpa'\n\npublic class Student {\n    public static void main(String[] args) {\n        // Your code here\n        \n        \n        // Print the variables\n        System.out.println(\"Name: \" + name);\n        System.out.println(\"Age: \" + age);\n        System.out.println(\"GPA: \" + gpa);\n    }\n}",
        solution: "String name = \"Alice\";\nint age = 20;\ndouble gpa = 3.8;"
      }
    ]
  };

  const [quizAnswer, setQuizAnswer] = useState(null);
  const [showQuizResult, setShowQuizResult] = useState(false);

  const handleNext = () => {
    if (currentStep < lesson.steps.length - 1) {
      setCurrentStep(currentStep + 1);
      setQuizAnswer(null);
      setShowQuizResult(false);
      setCodeAnswer('');
    }
  };

  const handlePrevious = () => {
    if (currentStep > 0) {
      setCurrentStep(currentStep - 1);
      setQuizAnswer(null);
      setShowQuizResult(false);
    }
  };

  const handleQuizSubmit = () => {
    setShowQuizResult(true);
  };

  const runCode = () => {
    alert('Code execution simulated! In a real app, this would compile and run your Java code.');
  };

  const currentStepData = lesson.steps[currentStep];

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-4xl mx-auto px-4">
        {/* Header */}
        <div className="mb-8">
          <div className="text-sm text-gray-600 mb-2">
            <span>{lesson.course}</span> / <span>{lesson.title}</span>
          </div>
          <h1 className="text-3xl font-bold text-gray-900 mb-4">{lesson.title}</h1>
          <div className="flex gap-6 text-gray-600">
            <span>📅 {lesson.duration}</span>
            <span>📖 Step {currentStep + 1} of {lesson.steps.length}</span>
          </div>
        </div>

        {/* Progress Bar */}
        <div className="bg-gray-200 h-2 rounded-full mb-8 overflow-hidden">
          <div 
            className="bg-gradient-to-r from-blue-500 to-blue-600 h-full transition-all duration-300 ease-out"
            style={{ width: `${((currentStep + 1) / lesson.steps.length) * 100}%` }}
          ></div>
        </div>

        {/* Content */}
        <div className="bg-white rounded-xl shadow-lg p-8 mb-8">
          <h2 className="text-2xl font-semibold text-gray-900 mb-6">{currentStepData.title}</h2>
          
          {/* Content Step */}
          {currentStepData.type === 'content' && (
            <div className="prose max-w-none">
              <style jsx>{`
                .prose h3 {
                  color: #3b82f6;
                  font-size: 1.5rem;
                  font-weight: 600;
                  margin-bottom: 1rem;
                }
                
                .prose h4 {
                  color: #1f2937;
                  font-size: 1.125rem;
                  font-weight: 600;
                  margin: 1.5rem 0 0.5rem 0;
                }
                
                .prose p {
                  color: #4b5563;
                  line-height: 1.7;
                  margin-bottom: 1rem;
                }
                
                .prose ul {
                  list-style-type: disc;
                  margin-left: 1.5rem;
                  margin-bottom: 1rem;
                }
                
                .prose li {
                  color: #4b5563;
                  margin-bottom: 0.5rem;
                }
                
                .prose pre {
                  background: #1f2937;
                  color: #f3f4f6;
                  padding: 1.5rem;
                  border-radius: 0.5rem;
                  overflow-x: auto;
                  margin: 1.5rem 0;
                  font-family: 'Courier New', monospace;
                }
                
                .data-types-grid {
                  display: grid;
                  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
                  gap: 1rem;
                  margin: 2rem 0;
                }
                
                .data-type {
                  background: #f8fafc;
                  border: 1px solid #e2e8f0;
                  padding: 1.5rem;
                  border-radius: 0.5rem;
                }
                
                .data-type h4 {
                  color: #3b82f6;
                  margin-top: 0;
                  margin-bottom: 0.5rem;
                }
                
                .data-type p {
                  color: #6b7280;
                  font-size: 0.875rem;
                  margin-bottom: 0.75rem;
                }
                
                .data-type code {
                  background: #1f2937;
                  color: #f3f4f6;
                  padding: 0.5rem;
                  border-radius: 0.25rem;
                  font-family: 'Courier New', monospace;
                  font-size: 0.875rem;
                  display: block;
                }
              `}</style>
              <div dangerouslySetInnerHTML={{ __html: currentStepData.content }} />
            </div>
          )}
          
          {/* Quiz Step */}
          {currentStepData.type === 'quiz' && (
            <div className="space-y-6">
              <h3 className="text-xl font-medium text-gray-900">{currentStepData.question}</h3>
              <div className="space-y-3">
                {currentStepData.options.map((option, index) => (
                  <label key={index} className="flex items-center p-4 border border-gray-200 rounded-lg hover:bg-gray-50 cursor-pointer transition-colors">
                    <input
                      type="radio"
                      name="quiz"
                      value={index}
                      checked={quizAnswer === index}
                      onChange={(e) => setQuizAnswer(parseInt(e.target.value))}
                      className="mr-3 text-blue-600"
                    />
                    <span className="text-gray-700">{option}</span>
                  </label>
                ))}
              </div>
              
              {!showQuizResult && (
                <button 
                  className="bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                  onClick={handleQuizSubmit}
                  disabled={quizAnswer === null}
                >
                  Submit Answer
                </button>
              )}
              
              {showQuizResult && (
                <div className={`p-4 rounded-lg ${quizAnswer === currentStepData.correct ? 'bg-green-50 text-green-800' : 'bg-red-50 text-red-800'}`}>
                  {quizAnswer === currentStepData.correct ? (
                    <div className="flex items-center">
                      <span className="mr-2">✅</span>
                      <span>Correct! Well done!</span>
                    </div>
                  ) : (
                    <div className="flex items-center">
                      <span className="mr-2">❌</span>
                      <span>Incorrect. The correct answer is: {currentStepData.options[currentStepData.correct]}</span>
                    </div>
                  )}
                </div>
              )}
            </div>
          )}
          
          {/* Coding Step */}
          {currentStepData.type === 'coding' && (
            <div className="space-y-6">
              <p className="text-gray-700 bg-blue-50 p-4 rounded-lg">{currentStepData.instructions}</p>
              
              <div className="border border-gray-200 rounded-lg overflow-hidden">
                <div className="bg-gray-100 px-4 py-2 flex items-center justify-between">
                  <span className="font-medium text-gray-700">💻 Code Editor</span>
                  <button 
                    className="bg-green-600 text-white px-4 py-1 rounded text-sm hover:bg-green-700 transition-colors"
                    onClick={runCode}
                  >
                    ▶️ Run Code
                  </button>
                </div>
                <textarea
                  className="w-full p-4 font-mono text-sm resize-none focus:outline-none focus:ring-2 focus:ring-blue-500"
                  value={codeAnswer || currentStepData.startingCode}
                  onChange={(e) => setCodeAnswer(e.target.value)}
                  rows={15}
                />
              </div>
              
              <details className="bg-gray-50 p-4 rounded-lg">
                <summary className="cursor-pointer text-blue-600 font-medium hover:text-blue-700">
                  💡 Show Solution
                </summary>
                <pre className="mt-4 bg-gray-900 text-gray-100 p-4 rounded-lg overflow-x-auto">
                  <code>{currentStepData.solution}</code>
                </pre>
              </details>
            </div>
          )}
        </div>

        {/* Navigation */}
        <div className="flex items-center justify-between bg-white rounded-lg shadow p-6">
          <button 
            className="flex items-center px-6 py-2 text-gray-600 hover:text-gray-900 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
            onClick={handlePrevious}
            disabled={currentStep === 0}
          >
            ← Previous
          </button>
          
          <span className="text-gray-600 font-medium">
            {currentStep + 1} / {lesson.steps.length}
          </span>
          
          <button 
            className="flex items-center px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
            onClick={handleNext}
            disabled={currentStep === lesson.steps.length - 1}
          >
            Next →
          </button>
        </div>
      </div>
    </div>
  );
};

export default Lesson;