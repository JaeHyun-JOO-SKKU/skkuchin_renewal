import { CssBaseline, ThemeProvider, Container, } from '@mui/material';
import theme from '../theme/theme';
import logo from '../image/main_logo.png';
import Image from 'next/image';
import { useRouter } from "next/router";
import { useState, useEffect } from "react";
import loading0 from '../image/loading0.png';
import loading1 from '../image/loading1.png';
import loading2 from '../image/loading2.png';
import loading3 from '../image/loading3.png';
import { useDispatch, useSelector } from 'react-redux';
import { load_user_callback } from '../actions/auth/auth';

const loadingImages = [loading0, loading1, loading2, loading3];

export default function splash(){

    const isAuthenticated = useSelector(state => state.auth.isAuthenticated);
    const [loadingIndex, setLoadingIndex] = useState(0);
    const router = useRouter();
    const dispatch = useDispatch();

    useEffect(() => {
        /*
        setTimeout(() => {
            if (typeof window !== 'undefined') {
                if (isAuthenticated) {
                    router.push('/');
                } else {
                    router.push('/nextSplash');
                }
            }
        }, 10000);*/

        setTimeout(() => {
            if (dispatch && dispatch !== null && dispatch !== undefined) {
                dispatch(load_user_callback(([result, message]) => {
                    if (result) {
                        router.push('/');
                    } else {
                        if (message == 'authenticated_fail') {
                            let isUser = localStorage.getItem("user");
                            if (isUser == "true") {
                                router.push('/login')
                            } else {
                                localStorage.setItem("user", "true")
                                router.push('/nextSplash')
                            }
                        } else {
                            router.push('/')
                        }
                    }
                }))
            }
        }, 3000);

    }, []);

    useEffect(() => {
        const intervalId = setInterval(() => {
            setLoadingIndex((loadingIndex + 1) % loadingImages.length);
        
        }, 1000);
        return () => clearInterval(intervalId);
    }, [loadingIndex]);

    const height = window.innerHeight / 2 - 140;
    
    return(
        <ThemeProvider theme={theme}>
            <CssBaseline/>
            <Container style={{height:'100vh', width:'100%', position:'relative', padding:'0px', display:'flex'}}>
                <div style={{ width:'100%', height:'100%', textAlign:'center', position:'absolute', display:'block', justifyContent:'center', marginTop:height}}>
                    <Image src={logo} width={169} height={185}/>
                </div>
                <div style={{width:'100%', textAlign:'center', position:'absolute', bottom:55}}>
                    <Image src={loadingImages[loadingIndex] }width={133} height={30} layout='fixed' />
                </div>
            </Container>
        </ThemeProvider>
    )
}