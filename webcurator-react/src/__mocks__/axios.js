import { when } from "jest-when"

const targets = [
    {
        targetId: 1,
        creationDate: "2022-07-03T01:45:34.000+00:00",
        name: "test1",
        agency: "NLNZ",
        owner: "j. tester",
        state: 5,
        seeds: [
            {
                seed: "http://nlnz.govt.nz",
                primary: true
            }
        ]
    },
    {
        targetId: 2,
        creationDate: "2022-07-02T01:45:34.000+00:00",
        name: "test2",
        agency: "NLNZ",
        owner: "j. tester",
        state: 5,
        seeds: [
            {
                seed: "http://nlnz.govt.nz",
                primary: true
            }
        ]
    },
    {
        targetId: 3,
        creationDate: "2022-07-01T01:45:34.000+00:00",
        name: "test3",
        agency: "NLNZ",
        owner: "j. tester",
        state: 5,
        seeds: [
            {
                seed: "http://nlnz.govt.nz",
                primary: true
            }
        ]
    }
]

const mockResponse = {
    data: {
        targets: [
            {
                targetId: 1,
                creationDate: "2022-07-27T01:45:34.000+00:00",
                name: "test",
                agency: "NLNZ",
                owner: "j. tester",
                state: 5,
                seeds: [
                    {
                        seed: "http://nlnz.govt.nz",
                        primary: true
                    }
                ]
            },
            {
                targetId: 2,
                creationDate: "2022-07-27T01:45:34.000+00:00",
                name: "test",
                agency: "NLNZ",
                owner: "j. tester",
                state: 5,
                seeds: [
                    {
                        seed: "http://nlnz.govt.nz",
                        primary: true
                    }
                ]
            }
        ]
    }
}

const mockQueryResponse = {
    data: {
        targets: [
            {
                targetId: 1,
                creationDate: "2022-07-27T01:45:34.000+00:00",
                name: "test",
                agency: "NLNZ",
                owner: "j. tester",
                state: 5,
                seeds: [
                    {
                        seed: "http://test.govt.nz",
                        primary: true
                    }
                ]
            }
        ]
    }
}

const mockSortedDescResponse = {
    data: {
        targets: [
            {
                targetId: 1,
                creationDate: "2022-07-03T01:45:34.000+00:00",
                name: "test3",
                agency: "NLNZ",
                owner: "j. tester",
                state: 5,
                seeds: [
                    {
                        seed: "http://nlnz.govt.nz",
                        primary: true
                    }
                ]
            },
            {
                targetId: 2,
                creationDate: "2022-07-02T01:45:34.000+00:00",
                name: "test2",
                agency: "NLNZ",
                owner: "j. tester",
                state: 5,
                seeds: [
                    {
                        seed: "http://nlnz.govt.nz",
                        primary: true
                    }
                ]
            },
            {
                targetId: 3,
                creationDate: "2022-07-01T01:45:34.000+00:00",
                name: "test1",
                agency: "NLNZ",
                owner: "j. tester",
                state: 5,
                seeds: [
                    {
                        seed: "http://nlnz.govt.nz",
                        primary: true
                    }
                ]
            }
        ]
    }
}

const mockSortedAscResponse = {
    data: {
        targets: [
            {
                targetId: 3,
                creationDate: "2022-07-01T01:45:34.000+00:00",
                name: "test1",
                agency: "NLNZ",
                owner: "j. tester",
                state: 5,
                seeds: [
                    {
                        seed: "http://nlnz.govt.nz",
                        primary: true
                    }
                ]
            },
            {
                targetId: 2,
                creationDate: "2022-07-02T01:45:34.000+00:00",
                name: "test2",
                agency: "NLNZ",
                owner: "j. tester",
                state: 5,
                seeds: [
                    {
                        seed: "http://nlnz.govt.nz",
                        primary: true
                    }
                ]
            },
            {
                targetId: 1,
                creationDate: "2022-07-03T01:45:34.000+00:00",
                name: "test3",
                agency: "NLNZ",
                owner: "j. tester",
                state: 5,
                seeds: [
                    {
                        seed: "http://nlnz.govt.nz",
                        primary: true
                    }
                ]
            }
        ]
    }
}


const fn = jest.fn().mockResolvedValue(mockResponse)
when(fn)
    .calledWith('/wct/api/v1/targets').mockResolvedValue(mockResponse)
    .calledWith('/wct/api/v1/targets?seed=http://test.govt.nz').mockResolvedValue(mockQueryResponse)
    .calledWith('/wct/api/v1/targets?sortBy=name,desc').mockResolvedValue(mockSortedDescResponse)
    .calledWith('/wct/api/v1/targets?sortBy=name,asc').mockResolvedValue(mockSortedAscResponse)


export default {
    get: fn
}