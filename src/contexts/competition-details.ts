// import React, { createContext, useState } from "react";

// export const ContextContext = createContext({
//     contextInfo: [],
//     setContextInfo: () => {},
// });

// export const ContextProvider = ({ children }) => {
//     const [contextInfo, setContextInfo] = useState([]);

//     const setContextInfo = (file) => {
//         setContextInfo(file);
//     };

//     return (
//         <ContextContext.Provider
//             value={{
//                 contextInfo: contextInfo,
//                 setContextInfo: setContextInfo,
//             }}
//         >
//             {children}
//         </ContextContext.Provider>
//     );
// };
