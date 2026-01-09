interface Annotation {
    date: number | string,
    note: string,
    user: string,
    alert: boolean,
    targetInstanceId?: number
}

export type { Annotation }