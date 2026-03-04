import { createRouter, createWebHistory } from 'vue-router'

const routes = [
    {
        path: '/',
        name:'index',
        redirect: '/umanager/uhome'
    },
    {
        path: '/login',
        name: 'login',
        component: () => import('../views/login.vue')
    },
    {
        path: '/register',
        name: 'register',
        component: () => import('../views/register.vue')
    },
    {
        path: '/getback',
        name: 'getback',
        component: () => import('../views/getback.vue')
    },

    {
        path: '/umanager',
        name: 'umanager',
        component: () => import('../views/ForUser/Umanager.vue'),
        children: [
            {
                path: '',
                name: 'manager-uhome',
                redirect: 'uhome'
            },
            {
                path: 'uhome',
                name: 'manager-uhome',
                component: () => import('../views/ForUser/uhome.vue')
            },
            {
                path: 'random',
                name: 'manager-random',
                component: () => import('../views/ForUser/random.vue')
            },
            {
                path: 'special',
                name: 'manager-special',
                component: () => import('../views/ForUser/special.vue')
            },
            {
                path: 'result',
                name: 'manager-result',
                component: () => import('../views/ForUser/examResult.vue')
            },
            {
                path: 'simulation',
                name: 'manager-simulation',
                component: () => import('../views/ForUser/simulation.vue')
            },
            {
                path: 'center',
                name: 'manager-center',
                component: () => import('../views/ForUser/center.vue')
            }

        ]
    },

    {
        path: '/amanager',
        name: 'amanager',
        component: () => import('../views/ForAdmin/Amanager.vue'),
        children: [
            {
                path: '',
                name: 'manager-index',
                redirect: 'user'
            },
            {
                path: 'user',
                name: 'manager-user',
                component: () => import('../views/ForAdmin/user.vue')
            },
            {
                path: 'admin',
                name: 'manager-admin',
                component: () => import('../views/ForAdmin/admin.vue')
            },
            {
                path: 'question',
                name: 'manager-questionManager',
                component: () => import('../views/ForAdmin/questionManager.vue')
            }
        ]
    },
    {
        path: '/:pathMatch(.*)*',
        name: 'NotFound',
        component: () => import('../views/404.vue')
    }
]


const router = createRouter({
    history: createWebHistory(),
    routes
})

//路由守卫
const whiteList = [
    '/login',
    '/register',
    '/notfound',
    '/getback',
    '/umanager/uhome',
    '/umanager/one',
    '/umanager/four',
];

// 路由拦截逻辑（控制管理员页面访问权限）
router.beforeEach((to, from, next) => {
    const user = localStorage.getItem("login_user"); // 获取登录状态

    if (user) {
        next();
    } else {
        if (whiteList.includes(to.path)) {
            next();
        } else {
            next('/login');
        }
    }
})


export default router