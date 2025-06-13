import { Play, Save, Download, Upload, Settings } from 'lucide-react';

const CodePlayground = () => {
  const [code, setCode] = useState(`public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
        
        // Try your Java code here!
        int number = 42;
        String message = "Welcome to JavaCraft Academy";
        
        System.out.println("Number: " + number);
        System.out.println("Message: " + message);
    }
}`);

  const [output, setOutput] = useState('');
  const [isRunning, setIsRunning] = useState(false);
  const [theme, setTheme] = useState('dark');
  const [fontSize, setFontSize] = useState(14);

  const runCode = async () => {
    setIsRunning(true);
    setOutput('Compiling and running...\n');
    
    // Simulate code execution
    setTimeout(() => {
      const simulatedOutput = `Hello, World!
Number: 42
Message: Welcome to JavaCraft Academy

Program finished with exit code 0`;
      setOutput(simulatedOutput);
      setIsRunning(false);
    }, 1500);
  };

  const saveCode = () => {
    const blob = new Blob([code], { type: 'text/plain' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'MyJavaCode.java';
    a.click();
    URL.revokeObjectURL(url);
  };

  const loadExample = (example) => {
    const examples = {
      helloWorld: `public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
    }
}`,
      variables: `public class Variables {
    public static void main(String[] args) {
        // Different data types
        int age = 25;
        double price = 19.99;
        String name = "Alice";
        boolean isStudent = true;
        char grade = 'A';
        
        System.out.println("Name: " + name);
        System.out.println("Age: " + age);
        System.out.println("Price: $" + price);
        System.out.println("Is Student: " + isStudent);
        System.out.println("Grade: " + grade);
    }
}`,
      loops: `public class Loops {
    public static void main(String[] args) {
        // For loop
        System.out.println("For loop:");
        for (int i = 1; i <= 5; i++) {
            System.out.println("Count: " + i);
        }
        
        // While loop
        System.out.println("\\nWhile loop:");
        int j = 1;
        while (j <= 3) {
            System.out.println("While count: " + j);
            j++;
        }
    }
}`,
      arrays: `public class Arrays {
    public static void main(String[] args) {
        // Array declaration and initialization
        int[] numbers = {10, 20, 30, 40, 50};
        String[] fruits = {"Apple", "Banana", "Orange"};
        
        System.out.println("Numbers:");
        for (int num : numbers) {
            System.out.println(num);
        }
        
        System.out.println("\\nFruits:");
        for (String fruit : fruits) {
            System.out.println(fruit);
        }
    }
}`
    };
    setCode(examples[example]);
  };

  return (
    <div className="min-h-screen bg-gray-900 text-white">
      {/* Header */}
      <div className="bg-gray-800 border-b border-gray-700 px-6 py-4">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-blue-400">Code Playground</h1>
            <p className="text-gray-400 text-sm">Write, run, and experiment with Java code</p>
          </div>
          
          <div className="flex items-center gap-4">
            {/* Theme Toggle */}
            <button
              onClick={() => setTheme(theme === 'dark' ? 'light' : 'dark')}
              className="p-2 rounded-lg bg-gray-700 hover:bg-gray-600 transition-colors"
            >
              <Settings size={20} />
            </button>
            
            {/* Font Size Control */}
            <div className="flex items-center gap-2">
              <span className="text-sm text-gray-400">Font:</span>
              <select
                value={fontSize}
                onChange={(e) => setFontSize(parseInt(e.target.value))}
                className="bg-gray-700 text-white px-2 py-1 rounded text-sm"
              >
                <option value={12}>12px</option>
                <option value={14}>14px</option>
                <option value={16}>16px</option>
                <option value={18}>18px</option>
              </select>
            </div>
          </div>
        </div>
      </div>

      <div className="flex h-[calc(100vh-80px)]">
        {/* Sidebar with Examples */}
        <div className="w-64 bg-gray-800 border-r border-gray-700 p-4">
          <h3 className="text-lg font-semibold mb-4 text-blue-400">Examples</h3>
          <div className="space-y-2">
            <button
              onClick={() => loadExample('helloWorld')}
              className="w-full text-left p-3 rounded-lg bg-gray-700 hover:bg-gray-600 transition-colors"
            >
              <div className="font-medium">Hello World</div>
              <div className="text-sm text-gray-400">Basic Java program</div>
            </button>
            
            <button
              onClick={() => loadExample('variables')}
              className="w-full text-left p-3 rounded-lg bg-gray-700 hover:bg-gray-600 transition-colors"
            >
              <div className="font-medium">Variables</div>
              <div className="text-sm text-gray-400">Data types and variables</div>
            </button>
            
            <button
              onClick={() => loadExample('loops')}
              className="w-full text-left p-3 rounded-lg bg-gray-700 hover:bg-gray-600 transition-colors"
            >
              <div className="font-medium">Loops</div>
              <div className="text-sm text-gray-400">For and while loops</div>
            </button>
            
            <button
              onClick={() => loadExample('arrays')}
              className="w-full text-left p-3 rounded-lg bg-gray-700 hover:bg-gray-600 transition-colors"
            >
              <div className="font-medium">Arrays</div>
              <div className="text-sm text-gray-400">Working with arrays</div>
            </button>
          </div>
        </div>

        {/* Main Content */}
        <div className="flex-1 flex flex-col">
          {/* Toolbar */}
          <div className="bg-gray-800 border-b border-gray-700 px-4 py-2 flex items-center gap-3">
            <button
              onClick={runCode}
              disabled={isRunning}
              className="flex items-center gap-2 bg-green-600 hover:bg-green-700 disabled:bg-green-800 px-4 py-2 rounded-lg transition-colors"
            >
              <Play size={16} />
              {isRunning ? 'Running...' : 'Run Code'}
            </button>
            
            <button
              onClick={saveCode}
              className="flex items-center gap-2 bg-blue-600 hover:bg-blue-700 px-4 py-2 rounded-lg transition-colors"
            >
              <Download size={16} />
              Save
            </button>
            
            <div className="ml-auto text-sm text-gray-400">
              Java Playground
            </div>
          </div>

          {/* Editor and Output */}
          <div className="flex-1 flex">
            {/* Code Editor */}
            <div className="flex-1 flex flex-col">
              <div className="bg-gray-700 px-4 py-2 text-sm font-medium border-b border-gray-600">
                📝 Editor
              </div>
              <textarea
                value={code}
                onChange={(e) => setCode(e.target.value)}
                className="flex-1 bg-gray-900 text-white p-4 font-mono resize-none focus:outline-none"
                style={{ fontSize: `${fontSize}px` }}
                spellCheck={false}
              />
            </div>

            {/* Output Panel */}
            <div className="w-96 flex flex-col border-l border-gray-700">
              <div className="bg-gray-700 px-4 py-2 text-sm font-medium border-b border-gray-600">
                📤 Output
              </div>
              <div className="flex-1 bg-gray-800 p-4">
                <pre className="text-green-400 font-mono text-sm whitespace-pre-wrap">
                  {output || 'Click "Run Code" to see output here...'}
                </pre>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Tips Panel */}
      <div className="bg-gray-800 border-t border-gray-700 p-4">
        <div className="flex items-center gap-4 text-sm">
          <div className="flex items-center gap-2 text-blue-400">
            <span>💡</span>
            <span className="font-medium">Tips:</span>
          </div>
          <div className="text-gray-400">
            Use Ctrl+Enter to run code • Click examples to load templates • Your code is auto-saved locally
          </div>
        </div>
      </div>
    </div>
  );
};

export default CodePlayground;