interface Annotation {
    date: number,
    note: string,
    user: string,
    alert: boolean,
    targetInstanceId?: number
}

export type { Annotation }